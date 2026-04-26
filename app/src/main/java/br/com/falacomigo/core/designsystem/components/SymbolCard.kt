package br.com.falacomigo.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.falacomigo.core.designsystem.tokens.ColorTokens
import br.com.falacomigo.core.designsystem.tokens.SizeTokens
import br.com.falacomigo.core.model.SymbolUiModel

@Composable
fun SymbolCard(
    symbol: SymbolUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val backgroundColor = when {
        !enabled -> ColorTokens.SurfaceVariant.copy(alpha = 0.38f)
        isPressed -> ColorTokens.PressedBackground
        selected -> ColorTokens.PrimaryContainer
        else -> ColorTokens.SymbolCardBackground
    }

    val elevation = if (isPressed) SizeTokens.CardElevationPressed else SizeTokens.CardElevation

    val contentDesc = symbol.accessibilityLabel.ifEmpty { "${symbol.label}, símbolo de comunicação" }

    Card(
        modifier = modifier
            .size(SizeTokens.SymbolCardMinSize)
            .semantics {
                this.contentDescription = contentDesc
                this.role = Role.Button
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        shape = RoundedCornerShape(SizeTokens.BorderRadiusLarge),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        border = if (!enabled) null else BorderStroke(1.dp, ColorTokens.SymbolCardBorder)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(SizeTokens.SymbolImageMedium)
                        .clip(RoundedCornerShape(SizeTokens.BorderRadiusMedium))
                        .background(ColorTokens.PrimaryContainer.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (symbol.imagePath != null || symbol.imageUrl != null) {
                        // TODO: Load actual image with Coil
                    } else {
                        Text(
                            text = symbol.label.take(1),
                            style = MaterialTheme.typography.headlineMedium,
                            color = ColorTokens.OnPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = symbol.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (enabled) ColorTokens.OnSurface else ColorTokens.OnSurface.copy(alpha = 0.38f),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}