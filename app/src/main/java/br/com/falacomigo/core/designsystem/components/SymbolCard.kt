package br.com.falacomigo.core.designsystem.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.falacomigo.core.designsystem.tokens.ColorTokens
import br.com.falacomigo.core.model.SymbolUiModel
import coil.compose.AsyncImage

@Composable
fun SymbolCard(
    symbol: SymbolUiModel,
    imageResId: Int,
    isSpeaking: Boolean = false,
    isSmall: Boolean = false,
    vibrationEnabled: Boolean = true,
    onClick: () -> Unit
) {
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        label = "scale"
    )

    val baseColor = remember(symbol.category) { ColorTokens.getCardColor(symbol.category) }
    val textColor = remember(baseColor) { 
        val luminance = 0.299 * baseColor.red + 0.587 * baseColor.green + 0.114 * baseColor.blue
        if (luminance < 0.5) Color.White else Color.Black
    }

    // Otimização Geométrica: Removido Width/Height fixos que causavam conflito no Grid
    Column(
        modifier = Modifier
            .fillMaxWidth() // Ocupa exatamente a largura da coluna
            .aspectRatio(0.85f) // Mantém a proporção vertical perfeita
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(if (isSmall) 12.dp else 16.dp))
            .drawBehind {
                drawRect(if (isSpeaking) ColorTokens.FocusHighlight else baseColor)
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    if (vibrationEnabled) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onClick()
                }
            )
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(bottom = 2.dp)
                .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (imageResId != 0) {
                AsyncImage(
                    model = imageResId,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().padding(if (isSmall) 2.dp else 6.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                val categoryIcon = remember(symbol.category) { getCategoryIcon(symbol.category) }
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = null,
                    tint = textColor.copy(alpha = 0.8f),
                    modifier = Modifier.size(if (isSmall) 22.dp else 32.dp)
                )
            }
        }
        
        Text(
            text = symbol.label,
            fontSize = if (isSmall) 10.sp else 12.sp,
            fontWeight = FontWeight.Bold, // Bold é mais leve que Black para renderização
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = 1, // Reduzido para 1 linha para aliviar o motor de texto no scroll
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)
        )
    }
}

private fun getCategoryIcon(category: String) = when(category.lowercase()) {
    "social" -> Icons.Default.Chat
    "atividades" -> Icons.Default.Extension
    "lugares" -> Icons.Default.Place
    "acoes" -> Icons.Default.PlayArrow
    "rotina" -> Icons.Default.History
    "necessidades" -> Icons.Default.EmojiObjects
    "emocoes" -> Icons.Default.Face
    "emergencia" -> Icons.Default.Warning
    else -> Icons.Default.Favorite
}