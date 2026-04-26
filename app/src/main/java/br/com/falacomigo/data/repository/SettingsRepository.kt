package br.com.falacomigo.data.repository

import android.content.Context
import android.content.SharedPreferences
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
}