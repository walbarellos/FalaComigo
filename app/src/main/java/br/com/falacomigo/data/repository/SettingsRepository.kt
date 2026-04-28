package br.com.falacomigo.data.repository

import android.content.Context
import android.content.SharedPreferences
import br.com.falacomigo.core.model.BoardLayoutMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("fala_comigo_settings", Context.MODE_PRIVATE)

    private val _vibrationEnabled = MutableStateFlow(prefs.getBoolean("vibration_enabled", true))
    val vibrationEnabled: StateFlow<Boolean> = _vibrationEnabled.asStateFlow()

    private val _highContrastEnabled = MutableStateFlow(prefs.getBoolean("high_contrast_enabled", false))
    val highContrastEnabled: StateFlow<Boolean> = _highContrastEnabled.asStateFlow()

    private val _darkModeEnabled = MutableStateFlow(prefs.getBoolean("dark_mode_enabled", false))
    val darkModeEnabled: StateFlow<Boolean> = _darkModeEnabled.asStateFlow()

    private val _cardSizeScale = MutableStateFlow(prefs.getFloat("card_size_scale", 1.0f))
    val cardSizeScale: StateFlow<Float> = _cardSizeScale.asStateFlow()

    private val _voiceId = MutableStateFlow(prefs.getString("voice_id", null))
    val voiceId: StateFlow<String?> = _voiceId.asStateFlow()

    private val _voiceSpeechRate = MutableStateFlow(prefs.getFloat("voice_speech_rate", 0.90f))
    val voiceSpeechRate: StateFlow<Float> = _voiceSpeechRate.asStateFlow()

    private val _voicePitch = MutableStateFlow(prefs.getFloat("voice_pitch", 1.0f))
    val voicePitch: StateFlow<Float> = _voicePitch.asStateFlow()

    private val _voiceOfflineOnly = MutableStateFlow(prefs.getBoolean("voice_offline_only", true))
    val voiceOfflineOnly: StateFlow<Boolean> = _voiceOfflineOnly.asStateFlow()

    private val _boardLayoutMode = MutableStateFlow(BoardLayoutMode.fromId(prefs.getString("board_layout_mode", "grid")))
    val boardLayoutMode: StateFlow<BoardLayoutMode> = _boardLayoutMode.asStateFlow()

    fun setVibrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("vibration_enabled", enabled).apply()
        _vibrationEnabled.value = enabled
    }

    fun setHighContrastEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("high_contrast_enabled", enabled).apply()
        _highContrastEnabled.value = enabled
    }

    fun setDarkModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("dark_mode_enabled", enabled).apply()
        _darkModeEnabled.value = enabled
    }

    fun setCardSizeScale(scale: Float) {
        prefs.edit().putFloat("card_size_scale", scale).apply()
        _cardSizeScale.value = scale
    }

    fun setVoiceId(voiceId: String?) {
        prefs.edit().apply {
            if (voiceId == null) remove("voice_id") else putString("voice_id", voiceId)
        }.apply()
        _voiceId.value = voiceId
    }

    fun setVoiceSpeechRate(rate: Float) {
        val safeRate = rate.coerceIn(0.5f, 1.5f)
        prefs.edit().putFloat("voice_speech_rate", safeRate).apply()
        _voiceSpeechRate.value = safeRate
    }

    fun setVoicePitch(pitch: Float) {
        val safePitch = pitch.coerceIn(0.7f, 1.3f)
        prefs.edit().putFloat("voice_pitch", safePitch).apply()
        _voicePitch.value = safePitch
    }

    fun setVoiceOfflineOnly(offlineOnly: Boolean) {
        prefs.edit().putBoolean("voice_offline_only", offlineOnly).apply()
        _voiceOfflineOnly.value = offlineOnly
    }

    fun setBoardLayoutMode(mode: BoardLayoutMode) {
        prefs.edit().putString("board_layout_mode", mode.id).apply()
        _boardLayoutMode.value = mode
    }
}
