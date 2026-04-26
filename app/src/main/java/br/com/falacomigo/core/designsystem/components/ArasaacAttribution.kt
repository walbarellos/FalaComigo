package br.com.falacomigo.core.designsystem.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.falacomigo.core.designsystem.tokens.SpacingTokens

@Composable
fun ArasaacAttribution(
    modifier: Modifier = Modifier
) {
    Text(
        text = "Pictogramas ARASAAC — Governo de Aragão, licença CC BY-NC-SA. " +
                "Autor: Sergio Palao.",
        style = MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.Center,
        modifier = modifier.padding(SpacingTokens.Md)
    )
}