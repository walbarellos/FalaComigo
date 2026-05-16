package br.com.falacomigo.feature.settings

import androidx.lifecycle.ViewModel
import br.com.falacomigo.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.falacomigo.core.designsystem.tokens.ColorTokens
import br.com.falacomigo.core.designsystem.tokens.SpacingTokens
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AccessibilitySettingsState(
    val largeText: Boolean = false,
    val highContrast: Boolean = false
)

@HiltViewModel
class AccessibilitySettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val _state = MutableStateFlow(
        AccessibilitySettingsState(
            largeText = settingsRepository.cardSizeScale.value > 1.05f,
            highContrast = settingsRepository.highContrastEnabled.value
        )
    )
    val state: StateFlow<AccessibilitySettingsState> = _state.asStateFlow()

    fun setLargeText(enabled: Boolean) {
        settingsRepository.setCardSizeScale(if (enabled) 1.18f else 1.0f)
        _state.value = _state.value.copy(largeText = enabled)
    }

    fun setHighContrast(enabled: Boolean) {
        settingsRepository.setHighContrastEnabled(enabled)
        _state.value = _state.value.copy(highContrast = enabled)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibilitySettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AccessibilitySettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Acessibilidade", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ColorTokens.Surface)
            )
        },
        containerColor = ColorTokens.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorTokens.Background)
                .padding(paddingValues)
                .padding(SpacingTokens.Md)
        ) {
            SettingItem(
                title = "Texto grande",
                description = "Aumenta cards e rótulos da prancha",
                checked = state.largeText,
                onCheckedChange = viewModel::setLargeText
            )
            SettingItem(
                title = "Alto contraste",
                description = "Usa contraste mais forte no tema do app",
                checked = state.highContrast,
                onCheckedChange = viewModel::setHighContrast
            )
        }
    }
}

@Composable
private fun SettingItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = SpacingTokens.Sm),
        shape = RoundedCornerShape(16.dp),
        color = ColorTokens.Surface,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(SpacingTokens.Md)) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(description, style = MaterialTheme.typography.bodyMedium)
                }
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    colors = SwitchDefaults.colors(checkedThumbColor = ColorTokens.Primary)
                )
            }
        }
    }
}
