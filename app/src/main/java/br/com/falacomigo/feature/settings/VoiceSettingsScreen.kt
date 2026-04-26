package br.com.falacomigo.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.falacomigo.core.designsystem.tokens.ColorTokens
import br.com.falacomigo.core.designsystem.tokens.SpacingTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceSettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTtsHealth: () -> Unit,
    viewModel: VoiceSettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(SpacingTokens.Md)
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
                        text = if (state.isAvailable) "O app pode falar os símbolos" else "Verifique as configurações do sistema",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ColorTokens.OnSurfaceVariant
                    )
                }
            }

            Text(
                text = "Testar voz",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = SpacingTokens.Xl)
            )

            Button(
                onClick = { viewModel.testVoice() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = SpacingTokens.Sm)
            ) {
                Text("Ouvir: \"Olá, eu sou o Fala Comigo\"")
            }

            Text(
                text = "Velocidade",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = SpacingTokens.Xl)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = SpacingTokens.Sm),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { viewModel.setSpeed(1) },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = if (state.speedLevel == 1) ColorTokens.Primary else ColorTokens.SurfaceVariant
                    )
                ) {
                    Text(
                        "Lento",
                        color = if (state.speedLevel == 1) ColorTokens.OnPrimary else ColorTokens.OnSurface
                    )
                }
                Button(
                    onClick = { viewModel.setSpeed(2) },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = if (state.speedLevel == 2) ColorTokens.Primary else ColorTokens.SurfaceVariant
                    )
                ) {
                    Text(
                        "Normal",
                        color = if (state.speedLevel == 2) ColorTokens.OnPrimary else ColorTokens.OnSurface
                    )
                }
                Button(
                    onClick = { viewModel.setSpeed(3) },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = if (state.speedLevel == 3) ColorTokens.Primary else ColorTokens.SurfaceVariant
                    )
                ) {
                    Text(
                        " Rápido",
                        color = if (state.speedLevel == 3) ColorTokens.OnPrimary else ColorTokens.OnSurface
                    )
                }
            }

            Text(
                text = "Seleção: ${if (state.speedLevel == 1) "Lento" else if (state.speedLevel == 2) "Normal" else "Rápido"}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = SpacingTokens.Sm),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = SpacingTokens.Xl),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Voz ativada",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Desative para usar apenas símbolos visuais",
                        style = MaterialTheme.typography.bodySmall,
                        color = ColorTokens.OnSurfaceVariant
                    )
                }
                Switch(
                    checked = state.isAvailable,
                    onCheckedChange = { }
                )
            }
        }
    }
}