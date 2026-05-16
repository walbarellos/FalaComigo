package br.com.falacomigo.feature.diagnostics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.falacomigo.core.designsystem.tokens.ColorTokens
import br.com.falacomigo.core.model.SymbolUiModel
import br.com.falacomigo.core.tts.TtsController
import br.com.falacomigo.data.repository.SettingsRepository
import br.com.falacomigo.data.repository.SymbolRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class DiagnosticsState(
    val isLoading: Boolean = true,
    val totalSymbols: Int = 0,
    val criticalSymbols: Int = 0,
    val criticalReady: Int = 0,
    val ttsAvailable: Boolean = false,
    val offlineOnly: Boolean = true,
    val voiceCount: Int = 0,
    val offlineVoiceCount: Int = 0,
    val testVoiceSent: Boolean = false
)

@HiltViewModel
class DiagnosticsViewModel @Inject constructor(
    private val symbolRepository: SymbolRepository,
    private val settingsRepository: SettingsRepository,
    private val ttsController: TtsController
) : ViewModel() {
    private val _state = MutableStateFlow(DiagnosticsState())
    val state: StateFlow<DiagnosticsState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, testVoiceSent = false)
            val symbols = withContext(Dispatchers.IO) { symbolRepository.getAllSymbolsOnce() }
            val critical = symbols.filter { it.label in criticalLabels || it.isEmergency }
            val offlineOnly = settingsRepository.voiceOfflineOnly.value
            ttsController.refreshVoices(offlineOnly)
            val voices = ttsController.getAvailableVoices()

            _state.value = DiagnosticsState(
                isLoading = false,
                totalSymbols = symbols.size,
                criticalSymbols = critical.size,
                criticalReady = critical.count(::hasUsableOfflineImage),
                ttsAvailable = ttsController.isAvailable(),
                offlineOnly = offlineOnly,
                voiceCount = voices.size,
                offlineVoiceCount = voices.count { !it.isNetworkRequired }
            )
        }
    }

    fun testVoice() {
        viewModelScope.launch {
            runCatching { ttsController.speak("Eu quero água. Me ajuda. Quero parar.") }
            _state.value = _state.value.copy(testVoiceSent = true)
        }
    }

    private fun hasUsableOfflineImage(symbol: SymbolUiModel): Boolean {
        return symbol.imageResId != 0 ||
            !symbol.localImagePath.isNullOrBlank() ||
            !symbol.thumbnailPath.isNullOrBlank() ||
            symbol.imageUrl.isNullOrBlank()
    }

    private companion object {
        val criticalLabels = setOf(
            "Água",
            "Com sede",
            "Com fome",
            "Banheiro",
            "Dor",
            "Machucado",
            "Ajuda",
            "Quero Parar"
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineReadinessScreen(
    onNavigateBack: () -> Unit,
    viewModel: DiagnosticsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prontidão offline", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Atualizar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = ColorTokens.Background
    ) { paddingValues ->
        DiagnosticsContent(
            state = state,
            paddingValues = paddingValues,
            onTestVoice = viewModel::testVoice
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TtsHealthScreen(
    onNavigateBack: () -> Unit,
    viewModel: DiagnosticsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verificar voz", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = ColorTokens.Background
    ) { paddingValues ->
        DiagnosticsContent(
            state = state,
            paddingValues = paddingValues,
            onTestVoice = viewModel::testVoice,
            voiceOnly = true
        )
    }
}

@Composable
private fun DiagnosticsContent(
    state: DiagnosticsState,
    paddingValues: PaddingValues,
    onTestVoice: () -> Unit,
    voiceOnly: Boolean = false
) {
    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = ColorTokens.Primary)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorTokens.Background)
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!voiceOnly) {
            DiagnosticCard(
                icon = Icons.Default.CloudOff,
                title = "Símbolos essenciais",
                value = "${state.criticalReady} / ${state.criticalSymbols}",
                status = if (state.criticalSymbols > 0 && state.criticalReady == state.criticalSymbols) "Pronto" else "Atenção",
                good = state.criticalSymbols > 0 && state.criticalReady == state.criticalSymbols
            )
            DiagnosticCard(
                icon = Icons.Default.CheckCircle,
                title = "Catálogo local",
                value = "${state.totalSymbols} símbolos",
                status = if (state.totalSymbols > 0) "Disponível" else "Vazio",
                good = state.totalSymbols > 0
            )
        }

        DiagnosticCard(
            icon = Icons.Default.RecordVoiceOver,
            title = "Voz do aparelho",
            value = if (state.ttsAvailable) "Disponível" else "Indisponível",
            status = if (state.offlineOnly) "${state.offlineVoiceCount} vozes offline" else "${state.voiceCount} vozes",
            good = state.ttsAvailable
        )

        Button(
            onClick = onTestVoice,
            enabled = state.ttsAvailable,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ColorTokens.Primary)
        ) {
            Text("Testar voz", fontWeight = FontWeight.Bold)
        }

        if (state.testVoiceSent) {
            Text(
                text = "Teste enviado para o motor de voz.",
                color = ColorTokens.OnSurfaceVariant,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
private fun DiagnosticCard(
    icon: ImageVector,
    title: String,
    value: String,
    status: String,
    good: Boolean
) {
    val accent = if (good) Color(0xFF059669) else ColorTokens.Error
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(accent.copy(alpha = 0.10f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (good) icon else Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = accent
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = ColorTokens.OnSurface, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(2.dp))
                Text(value, color = ColorTokens.OnSurface, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                Text(status, color = ColorTokens.OnSurfaceVariant, fontSize = 12.sp)
            }
        }
    }
}
