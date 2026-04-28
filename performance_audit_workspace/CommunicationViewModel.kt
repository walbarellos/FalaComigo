package br.com.falacomigo.feature.communication

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.falacomigo.core.model.BoardLayoutMode
import br.com.falacomigo.core.model.BoardUiModel
import br.com.falacomigo.core.model.FavoritePhrase
import br.com.falacomigo.core.model.RoutineUiModel
import br.com.falacomigo.core.model.SymbolCategory
import br.com.falacomigo.core.model.SymbolUiModel
import br.com.falacomigo.core.seed.SeedBoards
import br.com.falacomigo.core.tts.TtsController
import br.com.falacomigo.data.repository.BoardRepository
import br.com.falacomigo.data.repository.RoutineRepository
import br.com.falacomigo.data.repository.SettingsRepository
import br.com.falacomigo.data.repository.SymbolRepository
import br.com.falacomigo.feature.communication.domain.MoveSymbolUseCase
import br.com.falacomigo.feature.communication.domain.SaveRoutineUseCase
import br.com.falacomigo.feature.communication.domain.SearchSymbolsUseCase
import br.com.falacomigo.feature.communication.domain.SpeakSymbolUseCase
import coil.SingletonImageLoader
import coil.request.ImageRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ---------------------------------------------------------------------------
// Actions (Intent)
// ---------------------------------------------------------------------------

sealed class CommunicationAction {
    data class SelectBoard(val id: String) : CommunicationAction()
    data class ClickSymbol(val symbol: SymbolUiModel) : CommunicationAction()
    data class MoveSymbol(val from: Int, val target: Int) : CommunicationAction()
    data class Search(val query: String) : CommunicationAction()
    data class SetLayoutMode(val mode: BoardLayoutMode) : CommunicationAction()
    object ToggleVibration : CommunicationAction()
    object WarmUpTts : CommunicationAction()
}

// ---------------------------------------------------------------------------
// State (Snapshot imutável)
// ---------------------------------------------------------------------------

data class CommunicationState(
    val currentBoard: BoardUiModel = BoardUiModel(id = "", title = ""),
    /**
     * Símbolos agrupados por categoria, pré-computados no pipeline de background.
     * CategoryStreamLayout consome isso diretamente — custo zero de CPU na UI thread.
     */
    val groupedSymbols: Map<SymbolCategory, List<SymbolUiModel>> = emptyMap(),
    val routines: List<RoutineUiModel> = emptyList(),
    val favorites: List<FavoritePhrase> = emptyList(),
    val vibrationEnabled: Boolean = true,
    val layoutMode: BoardLayoutMode = BoardLayoutMode.GRID,
    val speakingSymbolId: String? = null,
    val isSpeaking: Boolean = false,
    val searchResults: List<SymbolUiModel> = emptyList(),
    val isSearching: Boolean = false,
    val editingRoutine: RoutineUiModel? = null,
    val editingRoutineSymbols: List<SymbolUiModel> = emptyList(),
)

// ---------------------------------------------------------------------------
// ViewModel
// ---------------------------------------------------------------------------

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class CommunicationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ttsController: TtsController,
    private val settingsRepository: SettingsRepository,
    private val routineRepository: RoutineRepository,
    private val boardRepository: BoardRepository,
    private val symbolRepository: SymbolRepository,
    private val speakSymbolUseCase: SpeakSymbolUseCase,
    private val moveSymbolUseCase: MoveSymbolUseCase,
    private val searchSymbolsUseCase: SearchSymbolsUseCase,
    private val saveRoutineUseCase: SaveRoutineUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(CommunicationState(currentBoard = SeedBoards.defaultBoard))
    val state: StateFlow<CommunicationState> = _state.asStateFlow()

    private val _activeFilter = MutableStateFlow("comunicacao")
    private val _searchQuery = MutableSharedFlow<String>(extraBufferCapacity = 1)

    /**
     * Usa o singleton do FalaComigoApplication — mesmo loader que rememberAsyncImagePainter.
     * Preloads vão para o cache correto e os cards encontram cache hit imediato.
     *
     * ANTES: ImageLoader(context) criava loader isolado com cache separado.
     * Resultado: preload era descartado, imagens carregadas duas vezes, cache hit nunca ocorria.
     */
    private val imageLoader by lazy { SingletonImageLoader.get(context) }

    init {
        setupTtsListeners()
        observeSettings()
        observeData()
        observeSearch()
    }

    // ---------------------------------------------------------------------------
    // Reducer
    // ---------------------------------------------------------------------------

    private fun reduce(reducer: (CommunicationState) -> CommunicationState) {
        _state.update(reducer)
    }

    // ---------------------------------------------------------------------------
    // Observação de dados
    // ---------------------------------------------------------------------------

    private fun observeData() {
        // Única subscrição de routines — o combine abaixo já as expõe via state.
        // Removida a subscrição duplicada que disparava queries e recomposições extras.

        combine(
            _activeFilter,
            routineRepository.getAllRoutines().distinctUntilChanged(),
            symbolRepository.getAllSymbols().distinctUntilChanged(),
        ) { filter, routines, allSymbols ->
            Triple(filter, routines, allSymbols)
        }
            .flatMapLatest { (filter, routines, allSymbols) ->
                when {
                    filter == "recentes" -> {
                        symbolRepository.getRecentlyUsed().map { symbols ->
                            Triple(
                                BoardUiModel(id = "recentes", title = "Recentes", symbols = symbols, columns = 4),
                                routines,
                                symbols,
                            )
                        }
                    }

                    filter.startsWith("routine_") -> {
                        val rId = filter.removePrefix("routine_")
                        val routine = routines.find { it.id == rId }
                        if (routine != null) {
                            val routineSymbols =
                                routine.symbols.mapNotNull { sid -> allSymbols.find { it.id == sid } }
                            flowOf(
                                Triple(
                                    BoardUiModel(
                                        id = filter,
                                        title = routine.title,
                                        symbols = routineSymbols,
                                        columns = 4,
                                    ),
                                    routines,
                                    routineSymbols,
                                )
                            )
                        } else flowOf(null)
                    }

                    filter == "comunicacao" -> {
                        boardRepository.getBoardWithSymbolsFlow("comunicacao").map { board ->
                            if (board == null) return@map null
                            val firstSymbolIds =
                                routines.mapNotNull { it.symbols.firstOrNull() }.toSet()
                            val symbolsById =
                                allSymbols.filter { it.id in firstSymbolIds }.associateBy { it.id }
                            val routineSymbols = routines.map { r ->
                                val realSymbol =
                                    r.symbols.firstOrNull()?.let { symbolsById[it] }
                                SymbolUiModel(
                                    id = "routine_${r.id}",
                                    label = r.title,
                                    spokenText = "",
                                    categoryId = "rotina",
                                    imageUrl = realSymbol?.imageUrl,
                                    imageResId = realSymbol?.imageResId ?: 0,
                                    isCustom = true,
                                )
                            }
                            val symbols = routineSymbols + board.symbols
                            Triple(board.copy(symbols = symbols), routines, symbols)
                        }
                    }

                    else -> {
                        val filtered = allSymbols.filter { it.categoryId == filter }
                        val category = SymbolCategory.fromId(filter)
                        flowOf(
                            Triple(
                                BoardUiModel(
                                    id = filter,
                                    title = category.title,
                                    symbols = filtered,
                                    columns = 4,
                                ),
                                routines,
                                filtered,
                            )
                        )
                    }
                }
            }
            .flowOn(Dispatchers.Default)
            .onEach { triple ->
                if (triple == null) return@onEach
                val (board, routines, symbols) = triple
                // groupBy computado no Dispatchers.Default acima — zero custo na UI thread
                val grouped = symbols.groupBy { it.category }
                reduce {
                    it.copy(
                        currentBoard = board,
                        groupedSymbols = grouped,
                        routines = routines,
                    )
                }
                preloadImages(symbols)
            }
            .launchIn(viewModelScope)
    }

    /**
     * Preloading proativo usando o MESMO ImageLoader singleton do FalaComigoApplication.
     * enqueue() já é não-bloqueante; não precisa de coroutine wrapper.
     * Tamanho 192px = tamanho de render exato dos cards — sem upscale/downscale em runtime.
     */
    private fun preloadImages(symbols: List<SymbolUiModel>) {
        symbols.forEach { symbol ->
            val data: Any = when {
                !symbol.imageUrl.isNullOrEmpty() -> symbol.imageUrl!!
                symbol.imageResId != 0 -> symbol.imageResId
                else -> return@forEach
            }
            val request = ImageRequest.Builder(context)
                .data(data)
                .size(192)
                .memoryCacheKey("symbol_${symbol.id}")
                .build()
            imageLoader.enqueue(request)
        }
    }

    // ---------------------------------------------------------------------------
    // Dispatch (MVI gateway)
    // ---------------------------------------------------------------------------

    fun dispatch(action: CommunicationAction) {
        when (action) {
            is CommunicationAction.SelectBoard -> _activeFilter.value = action.id
            is CommunicationAction.ClickSymbol -> handleSymbolClick(action.symbol)
            is CommunicationAction.MoveSymbol -> handleMoveSymbol(action.from, action.target)
            is CommunicationAction.Search -> _searchQuery.tryEmit(action.query)
            is CommunicationAction.SetLayoutMode -> handleSetLayoutMode(action.mode)
            CommunicationAction.ToggleVibration -> handleToggleVibration()
            CommunicationAction.WarmUpTts -> handleWarmUpTts()
        }
    }

    // ---------------------------------------------------------------------------
    // Public bridge (chamado pela UI sem conhecer actions internas)
    // ---------------------------------------------------------------------------

    fun selectBoard(id: String) = dispatch(CommunicationAction.SelectBoard(id))
    fun onSymbolClick(symbol: SymbolUiModel) = dispatch(CommunicationAction.ClickSymbol(symbol))
    fun moveSymbol(from: Int, to: Int) = dispatch(CommunicationAction.MoveSymbol(from, to))
    fun onSearchQueryChanged(query: String) = dispatch(CommunicationAction.Search(query))
    fun setLayoutMode(mode: BoardLayoutMode) = dispatch(CommunicationAction.SetLayoutMode(mode))
    fun toggleVibration() = dispatch(CommunicationAction.ToggleVibration)
    fun warmUpTts() = dispatch(CommunicationAction.WarmUpTts)

    // ---------------------------------------------------------------------------
    // Handlers internos
    // ---------------------------------------------------------------------------

    private fun handleSetLayoutMode(mode: BoardLayoutMode) {
        viewModelScope.launch { settingsRepository.setBoardLayoutMode(mode) }
    }

    private fun handleSymbolClick(symbol: SymbolUiModel) {
        if (symbol.id.startsWith("routine_")) {
            selectBoard(symbol.id)
            return
        }
        viewModelScope.launch {
            reduce { it.copy(speakingSymbolId = symbol.id) }
            speakSymbolUseCase(symbol)
        }
    }

    private fun handleMoveSymbol(from: Int, target: Int) {
        val currentSymbols = _state.value.currentBoard.symbols.toMutableList()
        if (from !in currentSymbols.indices || target !in currentSymbols.indices) return
        val item = currentSymbols.removeAt(from)
        currentSymbols.add(target, item)
        reduce { it.copy(currentBoard = it.currentBoard.copy(symbols = currentSymbols)) }
        viewModelScope.launch {
            moveSymbolUseCase(
                boardId = _activeFilter.value,
                allRoutines = _state.value.routines,
                reorderedSymbols = currentSymbols,
            )
        }
    }

    fun openRoutineAsBoard(routine: RoutineUiModel) = selectBoard("routine_${routine.id}")

    fun startEditRoutine(routine: RoutineUiModel) {
        viewModelScope.launch {
            val symbols = symbolRepository.getSymbolsByIds(routine.symbols.toSet()).first()
            val sorted = routine.symbols.mapNotNull { sid -> symbols.find { it.id == sid } }
            reduce { it.copy(editingRoutine = routine, editingRoutineSymbols = sorted) }
        }
    }

    fun clearEditRoutine() {
        reduce { it.copy(editingRoutine = null, editingRoutineSymbols = emptyList()) }
    }

    fun saveSymbol(symbol: SymbolUiModel) {
        viewModelScope.launch { symbolRepository.saveSymbol(symbol.copy(isCustom = true)) }
    }

    fun saveRoutine(name: String, symbols: List<SymbolUiModel>) {
        viewModelScope.launch {
            saveRoutineUseCase(_state.value.editingRoutine, name, symbols)
            clearEditRoutine()
        }
    }

    fun deleteRoutine(routineId: String) {
        viewModelScope.launch { routineRepository.deleteRoutine(routineId) }
    }

    // ---------------------------------------------------------------------------
    // Observação de settings e search
    // ---------------------------------------------------------------------------

    private fun observeSearch() {
        _searchQuery
            .debounce(400L)
            .filter { it.length >= 2 }
            .onEach { query ->
                reduce { it.copy(isSearching = true) }
                val results = searchSymbolsUseCase(query)
                reduce { it.copy(searchResults = results, isSearching = false) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeSettings() {
        settingsRepository.vibrationEnabled
            .onEach { enabled -> reduce { it.copy(vibrationEnabled = enabled) } }
            .launchIn(viewModelScope)

        settingsRepository.boardLayoutMode
            .onEach { mode -> reduce { it.copy(layoutMode = mode) } }
            .launchIn(viewModelScope)
    }

    // ---------------------------------------------------------------------------
    // TTS
    // ---------------------------------------------------------------------------

    private fun setupTtsListeners() {
        ttsController.setOnSpeechProgressListener(
            onStart = { _ -> reduce { it.copy(isSpeaking = true) } },
            onDone = { _ -> reduce { it.copy(isSpeaking = false, speakingSymbolId = null) } },
            onError = { _ -> reduce { it.copy(isSpeaking = false, speakingSymbolId = null) } },
        )
    }

    private fun handleWarmUpTts() {
        viewModelScope.launch(Dispatchers.IO) { ttsController.warmUp() }
    }

    private fun handleToggleVibration() {
        viewModelScope.launch {
            settingsRepository.setVibrationEnabled(!_state.value.vibrationEnabled)
        }
    }
}
