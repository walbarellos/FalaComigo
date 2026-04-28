package br.com.falacomigo.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.falacomigo.core.designsystem.tokens.ColorTokens
import br.com.falacomigo.core.designsystem.tokens.SpacingTokens
import br.com.falacomigo.core.model.BoardLayoutMode
import br.com.falacomigo.feature.communication.CommunicationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAccessibility: () -> Unit,
    onNavigateToVoice: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToOffline: () -> Unit,
    onNavigateToEditor: () -> Unit,
    viewModel: CommunicationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
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
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // --- NOVA SEÇÃO: INTERFACE ---
            Text(
                "Interface e Visualização",
                style = MaterialTheme.typography.labelLarge,
                color = ColorTokens.Primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            Text(
                "Escolha como os símbolos são organizados na tela",
                style = MaterialTheme.typography.bodySmall,
                color = ColorTokens.OnSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LayoutOptionButton("Grade", state.layoutMode == BoardLayoutMode.GRID, { viewModel.setLayoutMode(BoardLayoutMode.GRID) }, Modifier.weight(1f))
                LayoutOptionButton("Foco", state.layoutMode == BoardLayoutMode.PAGER, { viewModel.setLayoutMode(BoardLayoutMode.PAGER) }, Modifier.weight(1f))
                LayoutOptionButton("MMO", state.layoutMode == BoardLayoutMode.MMO, { viewModel.setLayoutMode(BoardLayoutMode.MMO) }, Modifier.weight(1f))
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = ColorTokens.Outline.copy(alpha = 0.3f))

            // --- SEÇÃO EXISTENTE: VOZ ---
            Text(
                "Feedback e Voz",
                style = MaterialTheme.typography.labelLarge,
                color = ColorTokens.Primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            SettingsSwitchItem(
                title = "Vibração ao tocar",
                description = "Sentir um leve toque ao escolher um símbolo",
                checked = state.vibrationEnabled,
                onCheckedChange = { viewModel.toggleVibration() }
            )

            SettingsMenuItem(
                title = "Voz e Fala",
                description = "Velocidade, tom e vozes disponíveis",
                onClick = onNavigateToVoice
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = ColorTokens.Outline.copy(alpha = 0.3f))

            Text(
                "Administração",
                style = MaterialTheme.typography.labelLarge,
                color = ColorTokens.Primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            SettingsMenuItem(
                title = "Entrar no Editor",
                description = "Editar pranchas, símbolos e trocar imagens",
                onClick = onNavigateToEditor
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = ColorTokens.Outline.copy(alpha = 0.3f))

            Text(
                "Informações",
                style = MaterialTheme.typography.labelLarge,
                color = ColorTokens.Primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            SettingsMenuItem(
                title = "Sobre e Licença",
                description = "Informações do app e símbolos ARASAAC",
                onClick = onNavigateToAbout
            )
        }
    }
}

@Composable
fun LayoutOptionButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) ColorTokens.Primary else ColorTokens.SurfaceVariant
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text,
            color = if (selected) ColorTokens.OnPrimary else ColorTokens.OnSurface,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SettingsSwitchItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = ColorTokens.OnSurfaceVariant)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = ColorTokens.Primary)
            )
        }
    }
}

@Composable
fun SettingsMenuItem(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = ColorTokens.OnSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = ColorTokens.OnSurfaceVariant)
        }
    }
}
