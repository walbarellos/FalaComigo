package br.com.falacomigo.feature.communication

import android.util.Log
import androidx.lifecycle.ViewModel
import br.com.falacomigo.core.model.BoardUiModel
import br.com.falacomigo.core.model.SymbolUiModel
import br.com.falacomigo.core.model.Routine
import br.com.falacomigo.core.model.FavoritePhrase
import br.com.falacomigo.core.seed.SeedBoards
import br.com.falacomigo.core.tts.AndroidTtsController
import br.com.falacomigo.core.tts.TtsController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class CommunicationState(
    val currentBoard: BoardUiModel = BoardUiModel(id = "", title = ""),
    val isSpeaking: Boolean = false,
    val ttsAvailable: Boolean = true,
    val recentClickedSymbol: String? = null,
    val clickCount: Int = 0,
    val currentSize: CardSize = CardSize.MEDIUM,
    val voiceSpeed: Int = 2,
    val routines: List<Routine> = emptyList(),
    val favorites: List<FavoritePhrase> = emptyList(),
    val editingRoutine: Routine? = null
)

enum class CardSize(val scale: Float, val displayName: String) {
    SMALL(0.8f, "Pequeno"),
    MEDIUM(1.0f, "Medio"),
    LARGE(1.2f, "Grande"),
    EXTRA_LARGE(1.4f, "Extra Grande")
}

@HiltViewModel
class CommunicationViewModel @Inject constructor(
    private val ttsController: TtsController
) : ViewModel() {

    private val _state = MutableStateFlow(CommunicationState(currentBoard = SeedBoards.defaultBoard))
    val state: StateFlow<CommunicationState> = _state.asStateFlow()

    init {
        Log.d("FalaComigo", "CommunicationViewModel created")
    }

    fun onSymbolClick(symbol: SymbolUiModel) {
        Log.d("FalaComigo", "onSymbolClick: ${symbol.label} -> ${symbol.spokenText}")
        
        _state.update { it.copy(isSpeaking = true, recentClickedSymbol = symbol.id, clickCount = it.clickCount + 1) }
        
        try {
            val available = ttsController.isAvailable()
            Log.d("FalaComigo", "TTS available: $available")
            
            if (available) {
                ttsController.speak(symbol.spokenText)
                Log.d("FalaComigo", "ttsController.speak() called with: ${symbol.spokenText}")
                
                addToFavorites(symbol)
            } else {
                Log.w("FalaComigo", "TTS not available, skipping speech")
            }
        } catch (e: Exception) {
            Log.e("FalaComigo", "TTS Error: ${e.message}")
            e.printStackTrace()
        }
        
        _state.update { it.copy(isSpeaking = false) }
    }

    private fun addToFavorites(symbol: SymbolUiModel) {
        val current = _state.value.favorites.toMutableList()
        val existing = current.indexOfFirst { it.text == symbol.spokenText }
        
        if (existing >= 0) {
            val existingFav = current[existing]
            current[existing] = existingFav.copy(
                clickCount = existingFav.clickCount + 1,
                lastUsed = System.currentTimeMillis()
            )
        } else {
            current.add(0, FavoritePhrase(
                id = symbol.id,
                text = symbol.spokenText,
                clickCount = 1
            ))
        }
        
        current.sortByDescending { it.clickCount }
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

    fun increaseCardSize() {
        val currentIndex = CardSize.entries.indexOf(_state.value.currentSize)
        if (currentIndex < CardSize.entries.size - 1) {
            _state.update { it.copy(currentSize = CardSize.entries[currentIndex + 1]) }
        }
    }

    fun decreaseCardSize() {
        val currentIndex = CardSize.entries.indexOf(_state.value.currentSize)
        if (currentIndex > 0) {
            _state.update { it.copy(currentSize = CardSize.entries[currentIndex - 1]) }
        }
    }

    fun getCurrentSizeName(): String = _state.value.currentSize.displayName

    fun getCardSize(): Float = _state.value.currentSize.scale

    fun setVoiceSpeed(speed: Int) {
        _state.update { it.copy(voiceSpeed = speed) }
        try {
            val controller = ttsController as? AndroidTtsController
            when (speed) {
                1 -> controller?.setSpeedSlow()
                2 -> controller?.setSpeedNormal()
                3 -> controller?.setSpeedFast()
            }
        } catch (e: Exception) {
            Log.e("FalaComigo", "Speed Error: ${e.message}")
        }
    }

    fun createRoutine(name: String, symbols: List<String>) {
        val routine = Routine(
            id = System.currentTimeMillis().toString(),
            name = name,
            symbols = symbols
        )
        val updated = _state.value.routines + routine
        _state.update { it.copy(routines = updated) }
    }

    fun playRoutine(routine: Routine) {
        val text = routine.symbols.joinToString(" ")
        Log.d("FalaComigo", "Playing routine: $text")
        ttsController.speak(text)
    }

    fun deleteRoutine(routineId: String) {
        val updated = _state.value.routines.filter { it.id != routineId }
        _state.update { it.copy(routines = updated) }
    }

    fun updateRoutine(routineId: String, name: String, symbols: List<String>) {
        val updated = _state.value.routines.map {
            if (it.id == routineId) it.copy(name = name, symbols = symbols)
            else it
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