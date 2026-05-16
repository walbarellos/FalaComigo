package br.com.falacomigo.core.designsystem.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.falacomigo.core.model.SymbolUiModel
import br.com.falacomigo.core.model.resolveImageModel
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest

private data class SymbolCardTheme(
    val accent: Color,
    val bg: Color,
    val border: Color,
)

private fun symbolCardTheme(categoryId: String): SymbolCardTheme {
    return when (categoryId) {
        "necessidades", "basic" -> SymbolCardTheme(
            accent = Color(0xFF2563EB),
            bg = Color(0xFFEFF6FF),
            border = Color(0xFFBFDBFE)
        )
        "social" -> SymbolCardTheme(
            accent = Color(0xFF7C3AED),
            bg = Color(0xFFF5F3FF),
            border = Color(0xFFDDD6FE)
        )
        "saude", "emergencia" -> SymbolCardTheme(
            accent = Color(0xFFDC2626),
            bg = Color(0xFFFEF2F2),
            border = Color(0xFFFECACA)
        )
        "emocoes" -> SymbolCardTheme(
            accent = Color(0xFFEA580C),
            bg = Color(0xFFFFF7ED),
            border = Color(0xFFFED7AA)
        )
        "numeral" -> SymbolCardTheme(
            accent = Color(0xFF0891B2),
            bg = Color(0xFFECFEFF),
            border = Color(0xFFA5F3FC)
        )
        else -> SymbolCardTheme(
            accent = Color(0xFF64748B),
            bg = Color(0xFFF8FAFC),
            border = Color(0xFFE2E8F0)
        )
    }
}

@Composable
fun SymbolCard(
    symbol: SymbolUiModel,
    imageResId: Int = 0,
    isSpeaking: Boolean = false,
    isSmall: Boolean = false,
    vibrationEnabled: Boolean = true,
    parallaxOffset: Float = 0f,
    textScale: Float = 1.0f,
    highContrast: Boolean = false,
    onClick: () -> Unit,
) {
    val view = LocalView.current
    val context = LocalContext.current
    val theme = remember(symbol.categoryId) { symbolCardTheme(symbol.categoryId) }
    val shape = RoundedCornerShape(if (isSmall) 12.dp else 18.dp)
    val resolvedModel = remember(
        symbol.id,
        symbol.localImagePath,
        symbol.thumbnailPath,
        symbol.imageUrl,
        symbol.imagePath,
        symbol.imageResId,
        imageResId,
        isSmall
    ) {
        symbol.resolveImageModel(context, preferThumbnail = isSmall) ?: imageResId.takeIf { it != 0 }
    }
    val imageRequest = remember(symbol.id, resolvedModel, isSmall) {
        resolvedModel?.let {
            ImageRequest.Builder(context)
                .data(it)
                .crossfade(false)
                .allowHardware(true)
                .memoryCacheKey("symbol_${symbol.id}_${if (isSmall) "small" else "card"}")
                .build()
        }
    }
    val scale = if (isSpeaking) 0.96f else 1f
    val accentColor = if (highContrast) Color.Black else theme.accent
    val imageBackground = when {
        highContrast -> Color.White
        isSpeaking -> Color(0xFFE0E7FF)
        else -> theme.bg
    }
    val borderColor = if (highContrast) Color.Black else theme.border
    val labelColor = if (highContrast) Color.Black else Color(0xFF0F172A)
    val categoryTextColor = if (highContrast) Color.Black else Color(0xFF94A3B8)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(if (isSmall) 0.9f else 0.82f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(if (isSmall) 1.dp else 3.dp, shape, clip = false)
            .clip(shape)
            .background(Color.White)
            .border(if (highContrast) 2.dp else 1.dp, borderColor, shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                if (vibrationEnabled) {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                }
                onClick()
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(imageBackground)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isSmall) 3.dp else 4.dp)
                    .background(accentColor)
            )

            if (imageRequest != null) {
                SubcomposeAsyncImage(
                    model = imageRequest,
                    contentDescription = symbol.label,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxSize(if (isSmall) 0.72f else 0.78f)
                        .graphicsLayer { translationX = parallaxOffset },
                    contentScale = ContentScale.Fit,
                    loading = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            SymbolImageFallback(
                                symbol = symbol,
                                isSmall = isSmall,
                                accentColor = accentColor,
                                borderColor = borderColor
                            )
                        }
                    },
                    error = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            SymbolImageFallback(
                                symbol = symbol,
                                isSmall = isSmall,
                                accentColor = accentColor,
                                borderColor = borderColor
                            )
                        }
                    },
                    success = {
                        SubcomposeAsyncImageContent()
                    }
                )
            } else {
                SymbolImageFallback(
                    symbol = symbol,
                    isSmall = isSmall,
                    accentColor = accentColor,
                    borderColor = borderColor,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = if (isSmall) 6.dp else 12.dp, vertical = if (isSmall) 5.dp else 10.dp),
            verticalArrangement = Arrangement.spacedBy(if (isSmall) 1.dp else 4.dp)
        ) {
            Text(
                text = symbol.label,
                color = labelColor,
                fontSize = ((if (isSmall) 10f else 15f) * textScale.coerceIn(1.0f, 1.25f)).sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!isSmall) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(accentColor)
                    )
                    Text(
                        text = symbol.category.title.uppercase(),
                        color = categoryTextColor,
                        fontSize = (10f * textScale.coerceIn(1.0f, 1.18f)).sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun SymbolImageFallback(
    symbol: SymbolUiModel,
    isSmall: Boolean,
    accentColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(if (isSmall) 34.dp else 58.dp)
            .clip(RoundedCornerShape(if (isSmall) 10.dp else 16.dp))
            .background(Color.White.copy(alpha = 0.86f))
            .border(1.dp, borderColor, RoundedCornerShape(if (isSmall) 10.dp else 16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = symbol.category.icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(if (isSmall) 20.dp else 32.dp)
        )
    }
}
