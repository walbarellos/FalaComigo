package br.com.falacomigo.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.falacomigo.core.designsystem.tokens.ColorTokens
import br.com.falacomigo.core.designsystem.tokens.SpacingTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutLicenseScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sobre e Licença") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                            contentDescription = "Voltar"
                        )
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
            Text(
                text = "Fala Comigo",
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "Versão 1.0.0",
                style = MaterialTheme.typography.bodyMedium,
                color = ColorTokens.OnSurfaceVariant,
                modifier = Modifier.padding(top = SpacingTokens.Sm)
            )

            Text(
                text = "App de Comunicação Aumentativa e Alternativa (CAA) para todos se comunicarem com símbolos visuais e fala.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = SpacingTokens.Lg)
            )

            Text(
                text = "Licença",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = SpacingTokens.Xl)
            )

            Text(
                text = "Este app é software livre. Você pode usar, modificar e distribuir.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = SpacingTokens.Sm)
            )

            Text(
                text = "Feito com ❤️ para comunicação acessível.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = SpacingTokens.Xl)
            )

            Text(
                text = "Créditos",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = SpacingTokens.Xl)
            )

            Text(
                text = "@walbarellos — Designer e Idealizador",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = SpacingTokens.Sm)
            )

            Text(
                text = "Símbolos ARASAAC (Aragon CAT)",
                style = MaterialTheme.typography.bodySmall,
                color = ColorTokens.OnSurfaceVariant,
                modifier = Modifier.padding(top = SpacingTokens.Md)
            )
        }
    }
}