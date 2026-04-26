package br.com.falacomigo.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import br.com.falacomigo.core.designsystem.tokens.ColorTokens
import br.com.falacomigo.core.designsystem.tokens.SpacingTokens

@Composable
fun PhraseBar(
    phraseText: String,
    onSpeak: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (phraseText.isEmpty()) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(ColorTokens.Surface)
            .padding(SpacingTokens.Md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = phraseText,
                style = MaterialTheme.typography.bodyLarge,
                color = ColorTokens.OnSurface,
                modifier = Modifier.weight(1f)
            )

            Row {
                TextButton(onClick = onClear) {
                    Text("Limpar")
                }
                TextButton(onClick = onSpeak) {
                    Text("Falar")
                }
            }
        }
    }
}

@Composable
fun PhraseBarCollapsed(
    onExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(ColorTokens.Surface)
            .padding(horizontal = SpacingTokens.Md),
        contentAlignment = Alignment.CenterStart
    ) {
        TextButton(onClick = onExpand) {
            Text("Modo frase")
        }
    }
}