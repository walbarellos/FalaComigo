package br.com.falacomigo.feature.communication

import android.content.Context
import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.falacomigo.core.model.BoardLayoutMode
import br.com.falacomigo.core.model.BoardUiModel
import br.com.falacomigo.core.model.FavoritePhrase
import br.com.falacomigo.core.model.RoutineUiModel
import br.com.falacomigo.core.model.SymbolCategory
import br.com.falacomigo.core.model.SymbolUiModel
import br.com.falacomigo.core.seed.SeedBoards
import br.com.falacomigo.core.seed.SeedSymbols
import br.com.falacomigo.core.tts.TtsController
import br.com.falacomigo.data.images.SymbolImageStore
import br.com.falacomigo.data.repository.BoardRepository
import br.com.falacomigo.data.repository.RoutineRepository
import br.com.falacomigo.data.repository.SettingsRepository
import br.com.falacomigo.data.repository.SymbolRepository
import br.com.falacomigo.feature.communication.domain.MoveSymbolUseCase
import br.com.falacomigo.feature.communication.domain.SaveRoutineUseCase
import br.com.falacomigo.feature.communication.domain.SearchSymbolsUseCase
import br.com.falacomigo.feature.communication.domain.SpeakSymbolUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class CommunicationAction {
    data class SelectBoard(val id: String) : CommunicationAction()
    data class ClickSymbol(val symbol: SymbolUiModel) : CommunicationAction()
    data class MoveSymbol(val from: Int, val target: Int) : CommunicationAction()
    data class Search(val query: String) : CommunicationAction()
    data class SetLayoutMode(val mode: BoardLayoutMode) : CommunicationAction()
    object ToggleVibration : CommunicationAction()
    object WarmUpTts : CommunicationAction()
}

data class CommunicationState(
    val currentBoard: BoardUiModel = BoardUiModel(id = "", title = ""),
    val isBootstrappingImages: Boolean = false,
    val bootstrapProgress: Float = 0f,
    val readyImageCount: Int = 0,
    val totalCriticalImages: Int = 0,
    val catalogSymbols: List<SymbolUiModel> = emptyList(),
    val groupedSymbols: Map<SymbolCategory, List<SymbolUiModel>> = emptyMap(),
    val routines: List<RoutineUiModel> = emptyList(),
    val favorites: List<FavoritePhrase> = emptyList(),
    val vibrationEnabled: Boolean = true,
    val speakOnTapEnabled: Boolean = true,
    val layoutMode: BoardLayoutMode = BoardLayoutMode.GRID,
    val speakingSymbolId: String? = null,
    val isSpeaking: Boolean = false,
    val speechErrorMessage: String? = null,
    val searchResults: List<SymbolUiModel> = emptyList(),
    val isSearching: Boolean = false,
    val editingRoutine: RoutineUiModel? = null,
    val editingRoutineSymbols: List<SymbolUiModel> = emptyList(),
    val editorPin: String = "1234",
    val highContrastEnabled: Boolean = false,
    val cardSizeScale: Float = 1.0f
)

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
    private val imageStore: SymbolImageStore
) : ViewModel() {

    private val _state = MutableStateFlow(CommunicationState())
    val state: StateFlow<CommunicationState> = _state.asStateFlow()

    private val _activeFilter = MutableStateFlow("comunicacao")
    private val _searchQuery = MutableSharedFlow<String>(extraBufferCapacity = 1)

    private var backgroundPrefetchJob: Job? = null
    private var warmUpJob: Job? = null
    private var lastWarmUpUptimeMs = 0L
    private val warmUpCooldownMs = 1_500L
    private val imageStatusFailed = "FAILED"

    init {
        setupTtsListeners()
        observeSettings()
        observeData()
        observeSearch()
    }

    private fun reduce(reducer: (CommunicationState) -> CommunicationState) {
        _state.update(reducer)
    }

    private fun observeData() {
        routineRepository.getAllRoutines()
            .distinctUntilChanged()
            .onEach { routines -> reduce { it.copy(routines = routines) } }
            .launchIn(viewModelScope)

        combine(
            _activeFilter,
            routineRepository.getAllRoutines().distinctUntilChanged(),
            symbolRepository.getAllSymbols().distinctUntilChanged()
        ) { filter, routines, allSymbols ->
            reduce { it.copy(catalogSymbols = allSymbols) }
            Triple(filter, routines, allSymbols)
        }
        .flatMapLatest { (filter, routines, allSymbols) ->
            when {
                filter == "recentes" -> {
                    symbolRepository.getRecentlyUsed().map { symbols ->
                        BoardUiModel(id = "recentes", title = "Recentes", symbols = symbols, columns = 4)
                    }
                }
                filter.startsWith("routine_") -> {
                    val rId = filter.removePrefix("routine_")
                    val routine = routines.find { it.id == rId }
                    if (routine != null) {
                        val routineSymbols = routine.symbols.mapNotNull { sid -> allSymbols.find { it.id == sid } }
                        flowOf(BoardUiModel(id = filter, title = routine.title, symbols = routineSymbols, columns = 4))
                    } else flowOf(null)
                }
                filter == "comunicacao" -> {
                    boardRepository.getBoardWithSymbolsFlow("comunicacao").map { board ->
                        val communicationBoard = board?.takeIf { it.symbols.isNotEmpty() }
                            ?: seedBoardFallback("comunicacao", allSymbols)
                            ?: return@map null
                        
                        // Busca IDs já presentes na prancha principal para evitar duplicatas
                        val existingIds = communicationBoard.symbols.map { it.id }.toSet()
                        
                        // Símbolos customizados que NÃO estão na prancha nem são rotinas
                        val customSymbols = allSymbols.filter { 
                            it.isCustom && it.id !in existingIds && !it.id.startsWith("routine_") 
                        }

                        val firstSymbolIds = routines.mapNotNull { it.symbols.firstOrNull() }.toSet()
                        val symbolsById = allSymbols.filter { it.id in firstSymbolIds }.associateBy { it.id }
                        val routineSymbols = routines.map { r ->
                            val realSymbol = r.symbols.firstOrNull()?.let { symbolsById[it] }
                            SymbolUiModel(
                                id = "routine_${r.id}", 
                                label = r.title, 
                                spokenText = "", 
                                categoryId = "rotina", 
                                imageUrl = realSymbol?.imageUrl, 
                                localImagePath = realSymbol?.localImagePath,
                                thumbnailPath = realSymbol?.thumbnailPath,
                                imageResId = realSymbol?.imageResId ?: 0, 
                                isCustom = true
                            )
                        }
                        // Junta tudo: Rotinas -> Símbolos da Prancha -> Novos Símbolos Customizados
                        communicationBoard.copy(symbols = routineSymbols + communicationBoard.symbols + customSymbols)
                    }
                }
                filter == "urgente" -> {
                    boardRepository.getBoardWithSymbolsFlow("urgente").map { board ->
                        board?.takeIf { it.symbols.isNotEmpty() }
                            ?: seedBoardFallback("urgente", allSymbols)
                            ?: BoardUiModel(
                                id = "urgente",
                                title = "Urgente",
                                symbols = allSymbols.filter {
                                    it.isEmergency || it.categoryId == "emergencia" || it.categoryId == "saude"
                                },
                                columns = 4,
                                isEmergency = true
                            )
                    }
                }
                filter == "personalizados" -> {
                    flowOf(BoardUiModel(
                        id = "personalizados",
                        title = "Personalizados",
                        symbols = allSymbols.filter { it.isCustom && !it.id.startsWith("routine_") },
                        columns = 4
                    ))
                }
                else -> {
                    val categoryIds = when (filter) {
                        "necessidades" -> listOf("necessidades", "basic", "saude")
                        "atividades" -> listOf("atividades", "lugares", "acoes")
                        "sensorial" -> listOf("sensorial", "emocoes")
                        else -> listOf(filter)
                    }
                    val filtered = allSymbols.filter { it.categoryId in categoryIds }
                    val category = SymbolCategory.fromId(filter)
                    val board = if (filtered.isNotEmpty()) {
                        BoardUiModel(id = filter, title = category.title, symbols = filtered, columns = 4)
                    } else {
                        seedBoardFallback(filter, allSymbols)
                            ?: BoardUiModel(id = filter, title = category.title, symbols = emptyList(), columns = 4)
                    }
                    flowOf(board)
                }
            }
        }
        .flowOn(Dispatchers.Default)
        .onEach { board ->
            if (board != null) {
                val grouped = board.symbols.groupBy { it.category }
                reduce { 
                    it.copy(
                        currentBoard = board, 
                        groupedSymbols = grouped,
                        isBootstrappingImages = false
                    ) 
                }
                scheduleBackgroundPrefetch(board)
            } else {
                val fallbackBoard = fallbackBoardForFilter(_activeFilter.value)
                reduce {
                    it.copy(
                        currentBoard = fallbackBoard,
                        groupedSymbols = emptyMap(),
                        isBootstrappingImages = false
                    )
                }
            }
        }
        .launchIn(viewModelScope)
    }

    private fun fallbackBoardForFilter(filter: String): BoardUiModel {
        val title = when {
            filter == "comunicacao" -> "Prancha"
            filter == "recentes" -> "Recentes"
            filter == "urgente" -> "Urgente"
            filter.startsWith("routine_") -> "Rotina"
            else -> SymbolCategory.fromId(filter).title
        }
        return BoardUiModel(id = filter, title = title, symbols = emptyList(), columns = 4)
    }

    private fun BoardUiModel.hasBlockingCriticalDownloads(): Boolean {
        return symbols
            .filter { symbol -> SeedSymbols.isCriticalOffline(symbol.id) }
            .any(::needsPersistentDownload)
    }

    private fun seedBoardFallback(boardId: String, allSymbols: List<SymbolUiModel>): BoardUiModel? {
        val seedBoard = SeedBoards.findById(boardId) ?: return null
        val symbolsById = allSymbols.associateBy { it.id }
        val symbols = seedBoard.symbols.map { seedSymbol ->
            symbolsById[seedSymbol.id] ?: seedSymbol
        }
        return seedBoard.copy(symbols = symbols)
    }

    private suspend fun prepareBoardForDisplay(board: BoardUiModel): BoardUiModel =
        withContext(Dispatchers.IO) {
            val critical = board.symbols.filter { symbol ->
                SeedSymbols.isCriticalOffline(symbol.id)
            }
            val pendingCritical = critical.filter(::needsPersistentDownload)

            if (pendingCritical.isNotEmpty()) {
                withContext(Dispatchers.Main.immediate) {
                    reduce {
                        it.copy(
                            isBootstrappingImages = true,
                            bootstrapProgress = 0f,
                            readyImageCount = 0,
                            totalCriticalImages = pendingCritical.size
                        )
                    }
                }

                pendingCritical.forEachIndexed { index, symbol ->
                    persistSymbolImage(symbol)
                    withContext(Dispatchers.Main.immediate) {
                        reduce {
                            it.copy(
                                readyImageCount = index + 1,
                                totalCriticalImages = pendingCritical.size,
                                bootstrapProgress = (index + 1) / pendingCritical.size.toFloat()
                            )
                        }
                    }
                }
            }

            hydrateBoard(board)
        }

    private fun needsPersistentDownload(symbol: SymbolUiModel): Boolean {
        if (symbol.id.startsWith("routine_")) return false
        if (symbol.imageDownloadStatus == imageStatusFailed) return false

        return symbol.imageUrl != null &&
            symbol.localImagePath.isNullOrBlank() &&
            symbol.thumbnailPath.isNullOrBlank() &&
            symbol.imageResId == 0
    }

    private fun List<SymbolUiModel>.prioritizedForFirstPaint(): List<SymbolUiModel> {
        val (priority, remaining) = partition { symbol ->
            SeedSymbols.isCriticalOffline(symbol.id)
        }
        return priority + remaining
    }

    private suspend fun persistSymbolImage(symbol: SymbolUiModel) {
        val url = symbol.imageUrl ?: return
        val stored = imageStore.ensureDownloaded(symbol.id, url) ?: run {
            symbolRepository.markImageFailed(symbol.id)
            return
        }

        symbolRepository.markLocalImageReady(
            symbolId = symbol.id,
            fullPath = stored.fullPath,
            thumbPath = stored.thumbnailPath
        )
    }

    private fun hydrateBoard(board: BoardUiModel): BoardUiModel {
        val hydrated = board.symbols.map { symbol ->
            val full = imageStore.getLocalFile(symbol.id)?.absolutePath ?: symbol.localImagePath
            val thumb = imageStore.getThumbnailFile(symbol.id)?.absolutePath ?: symbol.thumbnailPath
            symbol.copy(
                localImagePath = full,
                thumbnailPath = thumb
            )
        }
        return board.copy(symbols = hydrated)
    }

    private fun scheduleBackgroundPrefetch(board: BoardUiModel) {
        backgroundPrefetchJob?.cancel()
        backgroundPrefetchJob = viewModelScope.launch(Dispatchers.IO) {
            board.symbols
                .prioritizedForFirstPaint()
                .filter(::needsPersistentDownload)
                .forEach { persistSymbolImage(it) }
        }
    }

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

    // Public API Bridge
    fun selectBoard(id: String) = dispatch(CommunicationAction.SelectBoard(id))
    fun onSymbolClick(symbol: SymbolUiModel) = dispatch(CommunicationAction.ClickSymbol(symbol))
    fun moveSymbol(from: Int, to: Int) = dispatch(CommunicationAction.MoveSymbol(from, to))
    fun onSearchQueryChanged(query: String) = dispatch(CommunicationAction.Search(query))
    fun setLayoutMode(mode: BoardLayoutMode) = dispatch(CommunicationAction.SetLayoutMode(mode))
    fun toggleVibration() = dispatch(CommunicationAction.ToggleVibration)
    fun warmUpTts() = dispatch(CommunicationAction.WarmUpTts)

    fun clearSpeechError() {
        reduce { it.copy(speechErrorMessage = null) }
    }

    fun setSpeakOnTapEnabled(enabled: Boolean) {
        settingsRepository.setSpeakOnTapEnabled(enabled)
    }

    fun recordSymbolUse(symbol: SymbolUiModel) {
        if (symbol.id.startsWith("routine_")) return
        viewModelScope.launch(Dispatchers.IO) {
            symbolRepository.updateUsage(symbol.id)
        }
    }

    fun saveFavoritePhrase(text: String) {
        settingsRepository.saveFavoritePhrase(text)
    }

    fun deleteFavoritePhrase(id: String) {
        settingsRepository.deleteFavoritePhrase(id)
    }

    fun setEditorPin(pin: String) {
        settingsRepository.setEditorPin(pin)
    }

    fun setHighContrastEnabled(enabled: Boolean) {
        settingsRepository.setHighContrastEnabled(enabled)
    }

    fun setLargeTextEnabled(enabled: Boolean) {
        settingsRepository.setCardSizeScale(if (enabled) 1.18f else 1.0f)
    }

    fun onFavoritePhraseClick(favorite: FavoritePhrase) {
        settingsRepository.recordFavoritePhraseUse(favorite.id)
        onSymbolClick(SymbolUiModel(id = favorite.id, label = favorite.text, spokenText = favorite.text))
    }

    private fun handleSetLayoutMode(mode: BoardLayoutMode) {
        viewModelScope.launch { settingsRepository.setBoardLayoutMode(mode) }
    }

    private fun handleSymbolClick(symbol: SymbolUiModel) {
        if (symbol.id.startsWith("routine_")) {
            selectBoard(symbol.id)
            return
        }
        viewModelScope.launch {
            try {
                reduce { it.copy(speakingSymbolId = symbol.id, speechErrorMessage = null) }
                speakSymbolUseCase(symbol)
            } catch (e: Exception) {
                reduce {
                    it.copy(
                        speakingSymbolId = null,
                        isSpeaking = false,
                        speechErrorMessage = "Não consegui falar agora. Verifique Voz e Fala em Configurações."
                    )
                }
            }
        }
    }

    private fun handleMoveSymbol(from: Int, target: Int) {
        val currentSymbols = _state.value.currentBoard.symbols.toMutableList()
        if (from !in currentSymbols.indices || target !in currentSymbols.indices) return
        val item = currentSymbols.removeAt(from)
        currentSymbols.add(target, item)
        reduce { it.copy(currentBoard = it.currentBoard.copy(symbols = currentSymbols)) }
        viewModelScope.launch {
            moveSymbolUseCase(boardId = _activeFilter.value, allRoutines = _state.value.routines, reorderedSymbols = currentSymbols)
        }
    }

    fun openRoutineAsBoard(routine: RoutineUiModel) = selectBoard("routine_${routine.id}")

    fun startEditRoutine(routine: RoutineUiModel) {
        viewModelScope.launch {
            val symbols = symbolRepository.getSymbolsByIds(routine.symbols.toSet()).first()
            val sorted = routine.symbols.mapNotNull { sid -> symbols.find { it.id == sid } };
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

    private fun observeSearch() {
        _searchQuery.debounce(400L)
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

        settingsRepository.speakOnTapEnabled
            .onEach { enabled -> reduce { it.copy(speakOnTapEnabled = enabled) } }
            .launchIn(viewModelScope)

        settingsRepository.boardLayoutMode
            .onEach { mode -> reduce { it.copy(layoutMode = mode) } }
            .launchIn(viewModelScope)

        settingsRepository.favoritePhrases
            .onEach { favorites -> reduce { it.copy(favorites = favorites) } }
            .launchIn(viewModelScope)

        settingsRepository.editorPin
            .onEach { pin -> reduce { it.copy(editorPin = pin) } }
            .launchIn(viewModelScope)

        settingsRepository.highContrastEnabled
            .onEach { enabled -> reduce { it.copy(highContrastEnabled = enabled) } }
            .launchIn(viewModelScope)

        settingsRepository.cardSizeScale
            .onEach { scale -> reduce { it.copy(cardSizeScale = scale) } }
            .launchIn(viewModelScope)
    }

    private fun setupTtsListeners() {
        ttsController.setOnSpeechProgressListener(
            onStart = { _ -> reduce { it.copy(isSpeaking = true, speechErrorMessage = null) } },
            onDone = { _ -> reduce { it.copy(isSpeaking = false, speakingSymbolId = null) } },
            onError = { _ ->
                reduce {
                    it.copy(
                        isSpeaking = false,
                        speakingSymbolId = null,
                        speechErrorMessage = "Não consegui falar agora. Verifique Voz e Fala em Configurações."
                    )
                }
            }
        )
    }

    private fun handleWarmUpTts() {
        val now = SystemClock.uptimeMillis()
        if (warmUpJob?.isActive == true || now - lastWarmUpUptimeMs < warmUpCooldownMs) return
        lastWarmUpUptimeMs = now
        warmUpJob = viewModelScope.launch(Dispatchers.IO) { ttsController.warmUp() }
    }

    private fun handleToggleVibration() {
        viewModelScope.launch { settingsRepository.setVibrationEnabled(!_state.value.vibrationEnabled) }
    }
}
