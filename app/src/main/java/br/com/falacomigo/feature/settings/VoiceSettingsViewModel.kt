package br.com.falacomigo.feature.settings

import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.falacomigo.core.model.BoardLayoutMode
import br.com.falacomigo.core.tts.TtsController
import br.com.falacomigo.core.tts.TtsVoiceOption
import br.com.falacomigo.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VoiceSettingsState(
    val speedLevel: Int = 2,
    val pitchLevel: Int = 2,
    val offlineOnly: Boolean = true,
    val layoutMode: BoardLayoutMode = BoardLayoutMode.GRID,
    val isAvailable: Boolean = true,
    val availableVoices: List<TtsVoiceOption> = emptyList(),
    val selectedVoiceId: String? = null
)

@HiltViewModel
class VoiceSettingsViewModel @Inject constructor(
    private val ttsController: TtsController,
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(
        VoiceSettingsState(
            speedLevel = rateToSpeedLevel(settingsRepository.voiceSpeechRate.value),
            pitchLevel = pitchToPitchLevel(settingsRepository.voicePitch.value),
            offlineOnly = settingsRepository.voiceOfflineOnly.value,
            layoutMode = settingsRepository.boardLayoutMode.value,
            isAvailable = ttsController.isAvailable(),
            availableVoices = ttsController.getAvailableVoices(),
            selectedVoiceId = settingsRepository.voiceId.value ?: ttsController.getCurrentVoiceId()
        )
    )
    val state: StateFlow<VoiceSettingsState> = _state.asStateFlow()

    fun setLayoutMode(mode: BoardLayoutMode) {
        viewModelScope.launch {
            settingsRepository.setBoardLayoutMode(mode)
            _state.value = _state.value.copy(layoutMode = mode)
        }
    }

    fun refreshVoices() {
        refreshVoices(retryIfUnavailable = true)
    }

    private fun refreshVoices(retryIfUnavailable: Boolean) {
        ttsController.refreshVoices(_state.value.offlineOnly)
        _state.value = _state.value.copy(
            isAvailable = ttsController.isAvailable(),
            availableVoices = ttsController.getAvailableVoices(),
            selectedVoiceId = settingsRepository.voiceId.value ?: ttsController.getCurrentVoiceId()
        )
        if (retryIfUnavailable && !_state.value.isAvailable) {
            viewModelScope.launch {
                delay(600)
                refreshVoices(retryIfUnavailable = false)
            }
        }
    }

    fun testVoice() {
        viewModelScope.launch {
            try {
                ttsController.speak("Eu quero água. Me ajuda. Quero parar.")
            } catch (e: Exception) {
                println("TTS Error: ${e.message}")
            }
        }
    }

    fun setSpeed(level: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(speedLevel = level)
            ttsController.setSpeechRate(speedLevelToRate(level))
        }
    }

    fun setPitch(level: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(pitchLevel = level)
            ttsController.setPitch(pitchLevelToPitch(level))
        }
    }

    fun setOfflineOnly(offlineOnly: Boolean) {
        viewModelScope.launch {
            _state.value = _state.value.copy(offlineOnly = offlineOnly)
            ttsController.refreshVoices(offlineOnly)
            _state.value = _state.value.copy(
                availableVoices = ttsController.getAvailableVoices(),
                selectedVoiceId = settingsRepository.voiceId.value ?: ttsController.getCurrentVoiceId()
            )
        }
    }

    fun selectVoice(voiceId: String) {
        viewModelScope.launch {
            if (ttsController.selectVoice(voiceId)) {
                _state.value = _state.value.copy(selectedVoiceId = voiceId)
                testVoice()
            }
        }
    }

    fun isVoiceAvailable(): Boolean = ttsController.isAvailable()

    fun openTtsInstallScreen() {
        openSystemIntent(Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA))
    }

    fun openTtsSettings() {
        openSystemIntent(Intent("com.android.settings.TTS_SETTINGS"))
    }

    private fun openSystemIntent(intent: Intent) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            println("TTS settings intent error: ${e.message}")
        }
    }

    private companion object {
        fun speedLevelToRate(level: Int): Float = when (level) {
            1 -> 0.75f
            3 -> 1.10f
            else -> 0.90f
        }

        fun pitchLevelToPitch(level: Int): Float = when (level) {
            1 -> 0.90f
            3 -> 1.08f
            else -> 1.0f
        }

        fun rateToSpeedLevel(rate: Float): Int = when {
            rate < 0.85f -> 1
            rate > 1.0f -> 3
            else -> 2
        }

        fun pitchToPitchLevel(pitch: Float): Int = when {
            pitch < 0.96f -> 1
            pitch > 1.03f -> 3
            else -> 2
        }
    }
}
