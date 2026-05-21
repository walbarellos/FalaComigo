package br.com.falacomigo.feature.diagnostics

import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
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
import androidx.compose.material3.OutlinedButton
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
import br.com.falacomigo.core.seed.SeedSymbols
import br.com.falacomigo.core.tts.TtsController
import br.com.falacomigo.data.repository.SettingsRepository
import br.com.falacomigo.data.repository.SymbolRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

data class DiagnosticsState(
    val isLoading: Boolean = true,
    val totalSymbols: Int = 0,
    val criticalSymbols: Int = 0,
    val criticalReady: Int = 0,
    val totalImageSymbols: Int = 0,
    val readyOfflineImages: Int = 0,
    val pendingImageDownloads: Int = 0,
    val failedImageDownloads: Int = 0,
    val ttsAvailable: Boolean = false,
    val offlineOnly: Boolean = true,
    val voiceCount: Int = 0,
    val offlineVoiceCount: Int = 0,
    val testVoiceSent: Boolean = false,
    val voiceStatusMessage: String = "",
    val voiceTestMessage: String? = null
)

@HiltViewModel
class DiagnosticsViewModel @Inject constructor(
    private val symbolRepository: SymbolRepository,
    private val settingsRepository: SettingsRepository,
    private val ttsController: TtsController,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _state = MutableStateFlow(DiagnosticsState())
    val state: StateFlow<DiagnosticsState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh(retryIfUnavailable: Boolean = true) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                testVoiceSent = false,
                voiceTestMessage = null
            )
            val symbols = withContext(Dispatchers.IO) { symbolRepository.getAllSymbolsOnce() }
            val critical = symbols.filter(::isCriticalOfflineSymbol)
            val totalImageSymbols = symbols.count(::hasAnyImageSource)
            val readyOfflineImages = symbols.count { hasAnyImageSource(it) && hasUsableOfflineImage(it) }
            val failedImageDownloads = symbols.count { it.imageDownloadStatus == IMAGE_STATUS_FAILED && !hasUsableOfflineImage(it) }
            val pendingImageDownloads = symbols.count {
                it.imageDownloadStatus != IMAGE_STATUS_FAILED &&
                    !it.imageUrl.isNullOrBlank() &&
                    !hasUsableOfflineImage(it)
            }
            val offlineOnly = settingsRepository.voiceOfflineOnly.value
            ttsController.refreshVoices(offlineOnly)
            val voices = ttsController.getAvailableVoices()
            val ttsAvailable = ttsController.isAvailable()

            _state.value = DiagnosticsState(
                isLoading = false,
                totalSymbols = symbols.size,
                criticalSymbols = critical.size,
                criticalReady = critical.count(::hasUsableOfflineImage),
                totalImageSymbols = totalImageSymbols,
                readyOfflineImages = readyOfflineImages,
                pendingImageDownloads = pendingImageDownloads,
                failedImageDownloads = failedImageDownloads,
                ttsAvailable = ttsAvailable,
                offlineOnly = offlineOnly,
                voiceCount = voices.size,
                offlineVoiceCount = voices.count { !it.isNetworkRequired },
                voiceStatusMessage = buildVoiceStatusMessage(
                    ttsAvailable = ttsAvailable,
                    offlineOnly = offlineOnly,
                    voiceCount = voices.size,
                    offlineVoiceCount = voices.count { !it.isNetworkRequired }
                )
            )

            if (retryIfUnavailable && !ttsAvailable) {
                delay(TTS_RETRY_DELAY_MS)
                refresh(retryIfUnavailable = false)
            }
        }
    }

    fun testVoice() {
        viewModelScope.launch {
            if (!ttsController.isAvailable()) {
                ttsController.refreshVoices(settingsRepository.voiceOfflineOnly.value)
            }

            if (ttsController.isAvailable()) {
                runCatching { ttsController.speak("Eu quero água. Me ajuda. Quero parar.") }
                _state.value = _state.value.copy(
                    testVoiceSent = true,
                    voiceTestMessage = "Teste enviado para o motor de voz."
                )
            } else {
                _state.value = _state.value.copy(
                    testVoiceSent = false,
                    voiceTestMessage = "Voz indisponível no Android. Abra as configurações de voz ou instale os dados de TTS."
                )
            }
        }
    }

    fun openTtsSettings() {
        openSystemIntent(Intent("com.android.settings.TTS_SETTINGS"))
    }

    fun openTtsInstallScreen() {
        openSystemIntent(Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA))
    }

    private fun hasAnyImageSource(symbol: SymbolUiModel): Boolean {
        return symbol.imageResId != 0 ||
            !symbol.localImagePath.isNullOrBlank() ||
            !symbol.thumbnailPath.isNullOrBlank() ||
            !symbol.imagePath.isNullOrBlank() ||
            !symbol.imageUrl.isNullOrBlank()
    }

    private fun hasUsableOfflineImage(symbol: SymbolUiModel): Boolean {
        return symbol.imageResId != 0 ||
            symbol.localImagePath.isUsableFilePath() ||
            symbol.thumbnailPath.isUsableFilePath() ||
            symbol.imageUrl.isNullOrBlank()
    }

    private fun isCriticalOfflineSymbol(symbol: SymbolUiModel): Boolean {
        return SeedSymbols.isCriticalOffline(symbol.id) || symbol.isEmergency
    }

    private fun String?.isUsableFilePath(): Boolean {
        if (isNullOrBlank()) return false
        val file = File(this)
        return file.isFile && file.length() > 0L
    }

    private fun openSystemIntent(intent: Intent) {
        runCatching {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    private fun buildVoiceStatusMessage(
        ttsAvailable: Boolean,
        offlineOnly: Boolean,
        voiceCount: Int,
        offlineVoiceCount: Int
    ): String {
        if (!ttsAvailable) return "Motor de voz ainda não respondeu no Android."
        return when {
            offlineOnly && offlineVoiceCount > 0 -> "Pronto para fala offline em português."
            offlineOnly -> "Motor disponível, mas nenhuma voz offline pt-BR foi listada."
            voiceCount > 0 -> "Motor disponível com vozes pt-BR listadas."
            else -> "Motor disponível usando a voz padrão do aparelho."
        }
    }

    private companion object {
        const val TTS_RETRY_DELAY_MS = 700L
        const val IMAGE_STATUS_FAILED = "FAILED"
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
            onTestVoice = viewModel::testVoice,
            onOpenTtsSettings = viewModel::openTtsSettings,
            onOpenTtsInstallScreen = viewModel::openTtsInstallScreen
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
            onOpenTtsSettings = viewModel::openTtsSettings,
            onOpenTtsInstallScreen = viewModel::openTtsInstallScreen,
            voiceOnly = true
        )
    }
}

@Composable
private fun DiagnosticsContent(
    state: DiagnosticsState,
    paddingValues: PaddingValues,
    onTestVoice: () -> Unit,
    onOpenTtsSettings: () -> Unit,
    onOpenTtsInstallScreen: () -> Unit,
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
            DiagnosticCard(
                icon = Icons.Default.CloudOff,
                title = "Cache de imagens",
                value = "${state.readyOfflineImages} / ${state.totalImageSymbols}",
                status = "${state.pendingImageDownloads} pendentes · ${state.failedImageDownloads} falhas",
                good = state.totalImageSymbols == 0 ||
                    state.readyOfflineImages == state.totalImageSymbols ||
                    state.criticalReady == state.criticalSymbols,
                detail = if (state.criticalSymbols > 0 && state.criticalReady == state.criticalSymbols) {
                    "Fluxo crítico pode operar offline mesmo se imagens secundárias ainda baixarem."
                } else {
                    "Símbolos críticos ainda precisam de imagem local, drawable ou fallback seguro."
                }
            )
        }

        DiagnosticCard(
            icon = Icons.Default.RecordVoiceOver,
            title = "Voz do aparelho",
            value = if (state.ttsAvailable) "Disponível" else "Indisponível",
            status = if (state.offlineOnly) "${state.offlineVoiceCount} vozes offline" else "${state.voiceCount} vozes",
            good = state.ttsAvailable,
            detail = state.voiceStatusMessage
        )

        Button(
            onClick = onTestVoice,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ColorTokens.Primary)
        ) {
            Text("Testar voz", fontWeight = FontWeight.Bold)
        }

        state.voiceTestMessage?.let { message ->
            Text(
                text = message,
                color = ColorTokens.OnSurfaceVariant,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        OutlinedButton(
            onClick = onOpenTtsSettings,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Abrir configurações de voz do Android")
        }

        OutlinedButton(
            onClick = onOpenTtsInstallScreen,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Instalar dados de voz")
        }
    }
}

@Composable
private fun DiagnosticCard(
    icon: ImageVector,
    title: String,
    value: String,
    status: String,
    good: Boolean,
    detail: String? = null
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
                if (!detail.isNullOrBlank()) {
                    Text(detail, color = ColorTokens.OnSurfaceVariant, fontSize = 12.sp)
                }
            }
        }
    }
}
