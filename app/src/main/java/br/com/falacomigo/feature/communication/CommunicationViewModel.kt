package br.com.falacomigo.feature.communication

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.falacomigo.core.model.BoardUiModel
import br.com.falacomigo.core.model.FavoritePhrase
import br.com.falacomigo.core.model.Routine
import br.com.falacomigo.core.model.SymbolUiModel
import br.com.falacomigo.core.seed.SeedBoards
import br.com.falacomigo.core.seed.SeedSymbols
import br.com.falacomigo.core.tts.AndroidTtsController
import br.com.falacomigo.core.tts.TtsController
import br.com.falacomigo.data.repository.RoutineRepository
import br.com.falacomigo.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommunicationState(
    val currentBoard: BoardUiModel = BoardUiModel(id = "", title = ""),
    val isSpeaking: Boolean = false,
    val speakingSymbolId: String? = null,
    val ttsAvailable: Boolean = true,
    val voiceSpeed: Int = 2,
    val routines: List<Routine> = emptyList(),
    val favorites: List<FavoritePhrase> = emptyList(),
    val currentPhrase: List<SymbolUiModel> = emptyList(),
    val vibrationEnabled: Boolean = true,
    val editingRoutine: Routine? = null,
    // Otimização: Cache de IDs de imagem injetado no estado
    val imageIdCache: Map<String, Int> = emptyMap()
)

@HiltViewModel
class CommunicationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ttsController: TtsController,
    private val settingsRepository: SettingsRepository,
    private val routineRepository: RoutineRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CommunicationState(currentBoard = SeedBoards.defaultBoard))
    val state: StateFlow<CommunicationState> = _state.asStateFlow()

    private val SYMBOL_IMAGE_NAMES = mapOf(
        "eu" to "sym_eu", "voce" to "sym_voce", "feliz" to "sym_feliz", "triste" to "sym_triste",
        "bravo" to "sym_bravo", "com_medo" to "sym_com_medo", "cansado" to "sym_cansado",
        "frustrado" to "sym_frustrado", "com_fome" to "sym_com_fome", "com_sede" to "sym_com_sede",
        "dor" to "sym_dor", "machucado" to "sym_machucado", "banheiro" to "sym_banheiro",
        "agua" to "sym_agua", "ajuda" to "sym_ajuda", "quero_parar" to "sym_quero_parar"
    )

    init {
        precomputeImageIds()
        setupTtsListeners()
        observeVibration()
        seedInitialRoutines()
    }

    private fun precomputeImageIds() {
        val cache = SYMBOL_IMAGE_NAMES.mapValues { (_, name) ->
            context.resources.getIdentifier(name, "drawable", context.packageName)
        }
        _state.update { it.copy(imageIdCache = cache) }
    }

    private fun seedInitialRoutines() {
        if (_state.value.routines.isEmpty()) {
            val defaultRoutines = listOf(
                Routine(id = "1", name = "Rotina da Manhã", symbols = listOf("eu", "acordar", "banheiro", "escovar_dentes", "com_fome")),
                Routine(id = "2", name = "Rotina da Noite", symbols = listOf("tomar_banho", "vestir_roupa", "feliz", "quero_parar"))
            )
            _state.update { it.copy(routines = defaultRoutines) }
        }
    }

    private fun observeVibration() {
        viewModelScope.launch {
            settingsRepository.vibrationEnabled.collect { enabled ->
                _state.update { it.copy(vibrationEnabled = enabled) }
            }
        }
    }

    private fun setupTtsListeners() {
        ttsController.setOnSpeechProgressListener(
            onStart = { _ -> _state.update { it.copy(isSpeaking = true) } },
            onDone = { _ -> _state.update { it.copy(isSpeaking = false, speakingSymbolId = null) } },
            onError = { _ -> _state.update { it.copy(isSpeaking = false, speakingSymbolId = null) } }
        )
    }

    fun onSymbolClick(symbol: SymbolUiModel) {
        _state.update { 
            it.copy(
                speakingSymbolId = symbol.id,
                currentPhrase = it.currentPhrase + symbol
            ) 
        }
        if (ttsController.isAvailable()) {
            ttsController.speak(symbol.spokenText)
            addToFavorites(symbol)
        }
    }

    fun toggleVibration() {
        val currentValue = _state.value.vibrationEnabled
        settingsRepository.setVibrationEnabled(!currentValue)
    }

    private fun addToFavorites(symbol: SymbolUiModel) {
        val current = _state.value.favorites.toMutableList()
        val existing = current.indexOfFirst { it.text == symbol.spokenText }
        if (existing >= 0) {
            val existingFav = current[existing]
            current[existing] = existingFav.copy(clickCount = existingFav.clickCount + 1)
        } else {
            current.add(0, FavoritePhrase(id = symbol.id, text = symbol.spokenText, clickCount = 1))
        }
        _state.update { it.copy(favorites = current.take(16)) }
    }

    fun selectBoard(boardId: String) {
        SeedBoards.findById(boardId)?.let { board ->
            _state.update { it.copy(currentBoard = board) }
        }
    }

    fun openEmergencyBoard() {
        SeedBoards.boards.firstOrNull { it.isEmergency }?.let { board ->
            _state.update { it.copy(currentBoard = board) }
        }
    }

    fun setVoiceSpeed(speed: Int) {
        _state.update { it.copy(voiceSpeed = speed) }
        val controller = ttsController as? AndroidTtsController
        when (speed) {
            1 -> controller?.setSpeechRate(1.0f)
            2 -> controller?.setSpeechRate(1.5f)
            3 -> controller?.setSpeechRate(2.0f)
        }
    }

    fun playRoutine(routine: Routine) {
        ttsController.speak(routine.symbols.joinToString(" "))
    }

    fun deleteRoutine(routineId: String) {
        _state.update { it.copy(routines = it.routines.filter { r -> r.id != routineId }) }
    }

    fun createRoutine(name: String, symbols: List<String>) {
        val routine = Routine(id = System.currentTimeMillis().toString(), name = name, symbols = symbols)
        _state.update { it.copy(routines = it.routines + routine) }
    }

    fun updateRoutine(id: String, name: String, symbols: List<String>) {
        val updated = _state.value.routines.map {
            if (it.id == id) it.copy(name = name, symbols = symbols) else it
        }
        _state.update { it.copy(routines = updated) }
    }

    fun startEditRoutine(routine: Routine) {
        _state.update { it.copy(editingRoutine = routine) }
    }

    fun clearEditRoutine() {
        _state.update { it.copy(editingRoutine = null) }
    }

    override fun onCleared() {
        super.onCleared()
        ttsController.shutdown()
    }
}