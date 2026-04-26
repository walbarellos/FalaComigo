package br.com.falacomigo.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.falacomigo.core.tts.AndroidTtsController
import br.com.falacomigo.core.tts.TtsController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VoiceSettingsState(
    val speedLevel: Int = 2,
    val isAvailable: Boolean = true
)

@HiltViewModel
class VoiceSettingsViewModel @Inject constructor(
    private val ttsController: TtsController
) : ViewModel() {

    private val _state = MutableStateFlow(VoiceSettingsState(isAvailable = ttsController.isAvailable()))
    val state: StateFlow<VoiceSettingsState> = _state.asStateFlow()

    fun testVoice() {
        viewModelScope.launch {
            try {
                val message = when (_state.value.speedLevel) {
                    1 -> "Olá, eu sou o Fala Comigo. Estou falando devagar."
                    2 -> "Olá, eu sou o Fala Comigo."
                    3 -> "Olá, eu sou o Fala Comigo. Estou falando rápido."
                    else -> "Olá, eu sou o Fala Comigo."
                }
                ttsController.speak(message)
            } catch (e: Exception) {
                println("TTS Error: ${e.message}")
            }
        }
    }

    fun setSpeed(level: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(speedLevel = level)
            (ttsController as? AndroidTtsController)?.let { controller ->
                when (level) {
                    1 -> controller.setSpeedSlow()
                    2 -> controller.setSpeedNormal()
                    3 -> controller.setSpeedFast()
                }
            }
        }
    }

    fun isVoiceAvailable(): Boolean = ttsController.isAvailable()

    override fun onCleared() {
        super.onCleared()
        ttsController.shutdown()
    }
}