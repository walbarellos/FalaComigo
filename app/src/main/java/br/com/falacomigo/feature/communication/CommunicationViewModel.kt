package br.com.falacomigo.feature.communication

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.falacomigo.core.model.BoardUiModel
import br.com.falacomigo.core.model.FavoritePhrase
import br.com.falacomigo.core.model.RoutineUiModel
import br.com.falacomigo.core.model.SymbolUiModel
import br.com.falacomigo.core.seed.SeedBoards
import br.com.falacomigo.core.tts.TtsController
import br.com.falacomigo.data.remote.ArasaacApi
import br.com.falacomigo.data.repository.BoardRepository
import br.com.falacomigo.data.repository.RoutineRepository
import br.com.falacomigo.data.repository.SettingsRepository
import br.com.falacomigo.data.repository.SymbolRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommunicationState(
    val currentBoard: BoardUiModel = BoardUiModel(id = "", title = ""),
    val isSpeaking: Boolean = false,
    val speakingSymbolId: String? = null,
    val routines: List<RoutineUiModel> = emptyList(),
    val favorites: List<FavoritePhrase> = emptyList(),
    val vibrationEnabled: Boolean = true,
    val editingRoutine: RoutineUiModel? = null,
    val editingRoutineSymbols: List<SymbolUiModel> = emptyList(),
    val searchResults: List<SymbolUiModel> = emptyList(),
    val isSearching: Boolean = false
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
    private val arasaacApi: ArasaacApi
) : ViewModel() {

    private val _state = MutableStateFlow(CommunicationState(currentBoard = SeedBoards.defaultBoard))
    val state: StateFlow<CommunicationState> = _state.asStateFlow()

    // Controla o Filtro Ativo (ID da Categoria ou ID da Prancha/Rotina)
    private val _activeFilter = MutableStateFlow("comunicacao")
    private val _searchQuery = MutableSharedFlow<String>(extraBufferCapacity = 1)

    init {
        setupTtsListeners()
        observeVibration()
        observeData()
        observeSearchWithDebounce()
    }

    private fun observeData() {
        combine(_activeFilter, routineRepository.getAllRoutines(), symbolRepository.getAllSymbols()) { filter, routines, allSymbols ->
            _state.update { it.copy(routines = routines) }
            Triple(filter, routines, allSymbols)
        }.flatMapLatest { (filter, routines, allSymbols) ->
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
                    // PRANCHA PRINCIPAL: Carrega prancha "comunicacao" do banco + atalhos de rotina
                    boardRepository.getBoardWithSymbolsFlow("comunicacao").map { board ->
                        if (board == null) return@map null
                        
                        val firstSymbolIds = routines.mapNotNull { it.symbols.firstOrNull() }.toSet()
                        val symbolsById = allSymbols.filter { it.id in firstSymbolIds }.associateBy { it.id }

                        val routineSymbols = routines.map { r ->
                            val realSymbol = r.symbols.firstOrNull()?.let { symbolsById[it] }
                            SymbolUiModel(id = "routine_${r.id}", label = r.title, spokenText = "", category = "rotina", imageUrl = realSymbol?.imageUrl, imagePath = realSymbol?.imagePath, isCustom = true)
                        }
                        board.copy(symbols = routineSymbols + board.symbols)
                    }
                }
                else -> {
                    // FILTRO DINÂMICO POR CATEGORIA: Resolve o problema das tabelas vazias
                    // Busca todos os símbolos que pertencem a esta categoria no catálogo global
                    val filtered = allSymbols.filter { it.category == filter }
                    val title = getCategoryTitle(filter)
                    flowOf(BoardUiModel(id = filter, title = title, symbols = filtered, columns = 4))
                }
            }
        }.onEach { board ->
            if (board != null) _state.update { it.copy(currentBoard = board) }
        }.launchIn(viewModelScope)
    }

    private fun getCategoryTitle(cat: String) = when(cat) {
        "numeral" -> "Números"
        "social" -> "Social"
        "alimentacao" -> "Comer"
        "atividades" -> "Lazer"
        "necessidades" -> "Preciso"
        "emocoes" -> "Sentir"
        else -> "Filtrado"
    }

    fun selectBoard(id: String) { _activeFilter.value = id }

    fun onSymbolClick(symbol: SymbolUiModel) {
        viewModelScope.launch { if (!symbol.id.startsWith("routine_")) symbolRepository.updateUsage(symbol.id) }
        if (symbol.id.startsWith("routine_")) { selectBoard(symbol.id); return }
        val text = symbol.spokenText.ifEmpty { symbol.label }
        if (text.isNotBlank() && ttsController.isAvailable()) ttsController.speak(text)
        _state.update { it.copy(speakingSymbolId = symbol.id) }
    }

    fun moveSymbol(from: Int, to: Int) {
        val currentSymbols = _state.value.currentBoard.symbols.toMutableList()
        if (from !in currentSymbols.indices || to !in currentSymbols.indices) return
        val item = currentSymbols.removeAt(from)
        currentSymbols.add(to, item)
        _state.update { it.copy(currentBoard = it.currentBoard.copy(symbols = currentSymbols)) }

        viewModelScope.launch {
            val boardId = _activeFilter.value
            if (boardId.startsWith("routine_")) {
                val routineId = boardId.removePrefix("routine_")
                val routine = _state.value.routines.find { it.id == routineId }
                if (routine != null) routineRepository.saveRoutine(routine.copy(symbols = currentSymbols.map { it.id }))
            } else if (boardId == "comunicacao") {
                boardRepository.reorderBoardSymbols(boardId, currentSymbols)
            }
        }
    }

    fun openRoutineAsBoard(routine: RoutineUiModel) { selectBoard("routine_${routine.id}") }
    fun startEditRoutine(routine: RoutineUiModel) { viewModelScope.launch { val symbols = symbolRepository.getSymbolsByIds(routine.symbols.toSet()).first(); val sorted = routine.symbols.mapNotNull { sid -> symbols.find { it.id == sid } }; _state.update { it.copy(editingRoutine = routine, editingRoutineSymbols = sorted) } } }
    fun clearEditRoutine() { _state.update { it.copy(editingRoutine = null, editingRoutineSymbols = emptyList()) } }
    fun saveSymbol(symbol: SymbolUiModel) { viewModelScope.launch { symbolRepository.saveSymbol(symbol.copy(isCustom = true)) } }
    fun saveRoutine(name: String, symbols: List<SymbolUiModel>) { viewModelScope.launch { symbols.forEach { symbolRepository.saveSymbol(it.copy(isCustom = true)) }; val editingId = _state.value.editingRoutine?.id; val id = editingId ?: System.currentTimeMillis().toString(); val routine = RoutineUiModel(id = id, title = name, spokenText = "", symbols = symbols.map { it.id }, boardId = id); routineRepository.saveRoutine(routine); clearEditRoutine() } }
    fun onSearchQueryChanged(query: String) { if (query.length < 2) { _state.update { it.copy(searchResults = emptyList(), isSearching = false) }; return }; _searchQuery.tryEmit(query) }
    private fun observeSearchWithDebounce() { _searchQuery.debounce(400L).filter { it.length >= 2 }.distinctUntilChanged().onEach { query -> _state.update { it.copy(isSearching = true) }; try { val localResults = symbolRepository.getAllSymbolsOnce().filter { it.label.contains(query, ignoreCase = true) }; val response = arasaacApi.bestSearch(text = query); val remoteResults = response.map { picto -> SymbolUiModel(id = picto.id.toString(), label = query.replaceFirstChar { it.uppercase() }, spokenText = query, imageUrl = picto.imageUrl, category = "custom", isCustom = true) }; val combined = (localResults + remoteResults).distinctBy { it.id }; _state.update { it.copy(searchResults = combined, isSearching = false) } } catch (e: Exception) { _state.update { it.copy(isSearching = false, searchResults = emptyList()) } } }.launchIn(viewModelScope) }
    fun deleteRoutine(routineId: String) { viewModelScope.launch { routineRepository.deleteRoutine(routineId) } }
    fun toggleVibration() { viewModelScope.launch { settingsRepository.setVibrationEnabled(!_state.value.vibrationEnabled) } }
    private fun observeVibration() { settingsRepository.vibrationEnabled.onEach { enabled -> _state.update { it.copy(vibrationEnabled = enabled) } }.launchIn(viewModelScope) }
    private fun setupTtsListeners() { ttsController.setOnSpeechProgressListener(onStart = { _ -> _state.update { it.copy(isSpeaking = true) } }, onDone = { _ -> _state.update { it.copy(isSpeaking = false, speakingSymbolId = null) } }, onError = { _ -> _state.update { it.copy(isSpeaking = false, speakingSymbolId = null) } }) }
    override fun onCleared() { super.onCleared(); ttsController.shutdown() }
}