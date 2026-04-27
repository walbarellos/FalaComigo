package br.com.falacomigo.core.designsystem.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.falacomigo.core.designsystem.tokens.ColorTokens
import br.com.falacomigo.core.model.SymbolUiModel
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Size

/**
 * Card de símbolo da grade de comunicação.
 *
 * OTIMIZAÇÕES DE SCROLL (iOS-level):
 *
 * 1. Clipping correto: usa Modifier.clip(shape) ANTES do background.
 *    O graphicsLayer anterior com `shape` não existia na API — era no-op.
 *
 * 2. graphicsLayer { scaleX/Y } para animação de press.
 *    graphicsLayer é renderizado na GPU sem recomposição.
 *
 * 3. isSpeaking é tratado com animação suave (color + scale) para feedback
 *    visual sem recomposição pesada dos filhos.
 *
 * 4. memoryCacheKey explícita por ID + tamanho → cache hit garantido no scroll.
 *
 * 5. Sem crossfade por request — herda a config global do FalaComigoApplication.
 *
 * 6. Imagem com contentDescription = symbol.label para acessibilidade.
 */
@Composable
fun SymbolCard(
    symbol: SymbolUiModel,
    imageResId: Int = 0,
    isSpeaking: Boolean = false,
    isSmall: Boolean = false,
    vibrationEnabled: Boolean = true,
    onClick: () -> Unit
) {
    val view = LocalView.current
    val context = LocalContext.current

    // Shape estável — só recalculado quando isSmall muda
    val cornerRadius: Dp = if (isSmall) 10.dp else 14.dp
    val shape = remember(isSmall) { RoundedCornerShape(cornerRadius) }

    // Cor base por categoria — calculada uma vez por categoria
    val baseColor = remember(symbol.category) { ColorTokens.getCardColor(symbol.category) }

    // Animação de cor ao falar — suave, 150ms
    val cardColor by animateColorAsState(
        targetValue = if (isSpeaking) ColorTokens.FocusHighlight else baseColor,
        animationSpec = tween(durationMillis = 150),
        label = "card_color_${symbol.id}"
    )

    // Animação de escala ao falar — feedback visual tipo "pulso"
    val scale by animateFloatAsState(
        targetValue = if (isSpeaking) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "card_scale_${symbol.id}"
    )

    // Cor do texto calculada por luminância — memorizável
    val textColor = remember(baseColor) {
        val luminance = 0.299f * baseColor.red + 0.587f * baseColor.green + 0.114f * baseColor.blue
        if (luminance < 0.5f) Color.White else Color(0xFF1C1B1F)
    }

    // Ícone de fallback por categoria — memoizado
    val fallbackIcon = remember(symbol.category) { getCategoryIcon(symbol.category) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            // Proporção 4:5 — mais alta que larga, acomoda imagem + label confortavelmente
            .aspectRatio(0.82f)
            // ✅ CORRETO: graphicsLayer APENAS para transformações GPU (scale, alpha)
            // NÃO passar shape aqui — graphicsLayer.shape não existe na API Compose
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            // ✅ CORRETO: clipping via Modifier.clip, ANTES do background
            .clip(shape)
            .background(cardColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (vibrationEnabled) {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                }
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = if (isSmall) 4.dp else 8.dp,
                    vertical = if (isSmall) 4.dp else 8.dp
                )
        ) {
            // ── Área da imagem ──────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(if (isSmall) 6.dp else 10.dp))
                    .background(Color.White.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                // Resolve a fonte da imagem — prioridade: URL > resId > imagePath local
                val resolvedModel: Any? = remember(symbol.imageUrl, symbol.imagePath, imageResId) {
                    when {
                        !symbol.imageUrl.isNullOrEmpty() -> symbol.imageUrl
                        imageResId != 0 -> imageResId
                        else -> null
                    }
                }

                // imagePath precisa de context, não pode ficar dentro de remember
                val finalModel: Any? = resolvedModel
                    ?: run {
                        if (!symbol.imagePath.isNullOrEmpty()) {
                            val resId = context.resources.getIdentifier(
                                symbol.imagePath, "drawable", context.packageName
                            )
                            if (resId != 0) resId else null
                        } else null
                    }

                if (finalModel != null) {
                    // Tamanho de decode: 160px small, 220px normal
                    // Mínimo para fidelidade visual, máximo para performance de memória
                    val targetSize = if (isSmall) Size(160, 160) else Size(220, 220)

                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(finalModel)
                            // Cache key por ID + variante — garante hit no scroll de volta
                            .memoryCacheKey("sym_${symbol.id}_${if (isSmall) "s" else "n"}")
                            .diskCacheKey("sym_${symbol.id}")
                            .size(targetSize)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .build(),
                        contentDescription = symbol.label,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(if (isSmall) 3.dp else 7.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    // Fallback: ícone Material da categoria
                    Icon(
                        imageVector = fallbackIcon,
                        contentDescription = null,
                        tint = textColor.copy(alpha = 0.55f),
                        modifier = Modifier.size(if (isSmall) 22.dp else 34.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (isSmall) 2.dp else 5.dp))

            // ── Label ───────────────────────────────────────────────────────
            Text(
                text = symbol.label,
                fontSize = if (isSmall) 9.sp else 11.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = if (isSmall) 11.sp else 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (isSmall) 2.dp else 4.dp)
            )
        }
    }
}

// Memoizável pois é pura — resultado determinístico por input
private fun getCategoryIcon(category: String): ImageVector = when (category.lowercase()) {
    "social"       -> Icons.Default.Chat
    "atividades"   -> Icons.Default.Extension
    "lugares"      -> Icons.Default.Place
    "acoes"        -> Icons.Default.PlayArrow
    "rotina"       -> Icons.Default.History
    "necessidades" -> Icons.Default.EmojiObjects
    "emocoes"      -> Icons.Default.Face
    "emergencia"   -> Icons.Default.Warning
    "alimentacao"  -> Icons.Default.FoodBank
    "numeral"      -> Icons.Default.Tag
    else           -> Icons.Default.GridView
}