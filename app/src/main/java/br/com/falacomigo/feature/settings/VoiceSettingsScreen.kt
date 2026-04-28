package br.com.falacomigo.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.falacomigo.core.designsystem.tokens.ColorTokens
import br.com.falacomigo.core.designsystem.tokens.SpacingTokens
import br.com.falacomigo.core.model.BoardLayoutMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceSettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTtsHealth: () -> Unit,
    viewModel: VoiceSettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshVoices()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações de Voz") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
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
                .padding(SpacingTokens.ScreenPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (state.isAvailable) ColorTokens.PrimaryContainer else ColorTokens.ErrorContainer
                )
            ) {
                Column(modifier = Modifier.padding(SpacingTokens.Md)) {
                    Text(
                        text = if (state.isAvailable) "Voz disponível" else "Voz indisponível",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (state.isAvailable) "O app pode falar os símbolos" else "Verifique as configurações de voz do Android",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ColorTokens.OnSurfaceVariant
                    )
                }
            }

            SectionTitle("Voz instalada")
            Text(
                text = if (state.offlineOnly) {
                    "Mostrando apenas vozes pt-BR offline disponíveis no aparelho."
                } else {
                    "Mostrando vozes pt-BR offline e online disponíveis no aparelho."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = ColorTokens.OnSurfaceVariant,
                modifier = Modifier.padding(top = SpacingTokens.Sm)
            )

            if (state.availableVoices.isEmpty()) {
                Text(
                    text = "Nenhuma voz pt-BR foi encontrada. O Android usará a voz padrão do aparelho.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ColorTokens.OnSurfaceVariant,
                    modifier = Modifier.padding(top = SpacingTokens.Sm)
                )
            } else {
                state.availableVoices.forEach { voice ->
                    Button(
                        onClick = { viewModel.selectVoice(voice.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = SpacingTokens.Sm),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (state.selectedVoiceId == voice.id) ColorTokens.Primary else ColorTokens.SurfaceVariant
                        )
                    ) {
                        Text(
                            text = voice.name,
                            color = if (state.selectedVoiceId == voice.id) ColorTokens.OnPrimary else ColorTokens.OnSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            SectionTitle("Velocidade")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = SpacingTokens.Sm),
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.Sm)
            ) {
                OptionButton("Lenta", state.speedLevel == 1, { viewModel.setSpeed(1) }, Modifier.weight(1f))
                OptionButton("Normal", state.speedLevel == 2, { viewModel.setSpeed(2) }, Modifier.weight(1f))
                OptionButton("Rápida", state.speedLevel == 3, { viewModel.setSpeed(3) }, Modifier.weight(1f))
            }

            SectionTitle("Tom da voz")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = SpacingTokens.Sm),
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.Sm)
            ) {
                OptionButton("Grave", state.pitchLevel == 1, { viewModel.setPitch(1) }, Modifier.weight(1f))
                OptionButton("Normal", state.pitchLevel == 2, { viewModel.setPitch(2) }, Modifier.weight(1f))
                OptionButton("Suave", state.pitchLevel == 3, { viewModel.setPitch(3) }, Modifier.weight(1f))
            }

            SectionTitle("Testar voz")
            Button(
                onClick = { viewModel.testVoice() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = SpacingTokens.Sm)
            ) {
                Text("Ouvir frases essenciais")
            }

            SectionTitle("Avançado")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = SpacingTokens.Sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Usar apenas vozes offline",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Mantém a fala funcionando sem depender de internet",
                        style = MaterialTheme.typography.bodySmall,
                        color = ColorTokens.OnSurfaceVariant
                    )
                }
                Switch(
                    checked = state.offlineOnly,
                    onCheckedChange = { viewModel.setOfflineOnly(it) }
                )
            }

            OutlinedButton(
                onClick = { viewModel.openTtsSettings() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = SpacingTokens.Md)
            ) {
                Text("Abrir configurações de voz do Android")
            }

            OutlinedButton(
                onClick = { viewModel.openTtsInstallScreen() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = SpacingTokens.Sm)
            ) {
                Text("Instalar dados de voz")
            }

            OutlinedButton(
                onClick = onNavigateToTtsHealth,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = SpacingTokens.Sm)
                    .padding(bottom = SpacingTokens.Xxl)
            ) {
                Text("Diagnóstico de voz")
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = SpacingTokens.Xl)
    )
}

@Composable
private fun OptionButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) ColorTokens.Primary else ColorTokens.SurfaceVariant
        )
    ) {
        Text(
            text,
            color = if (selected) ColorTokens.OnPrimary else ColorTokens.OnSurface,
            textAlign = TextAlign.Center
        )
    }
}
