package br.com.falacomigo.core.designsystem.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.falacomigo.core.designsystem.tokens.ColorTokens
import br.com.falacomigo.core.model.SymbolUiModel
import br.com.falacomigo.core.model.resolveImageModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

/**
 * SymbolCard — Renderização direta via GPU com correções de corretude e performance.
 */
@Composable
fun SymbolCard(
    symbol: SymbolUiModel,
    imageResId: Int = 0,
    isSpeaking: Boolean = false,
    isSmall: Boolean = false,
    vibrationEnabled: Boolean = true,
    parallaxOffset: Float = 0f,
    onClick: () -> Unit,
) {
    val view = LocalView.current
    val context = LocalContext.current
    val textMeasurer = rememberTextMeasurer()

    val baseColor = symbol.category.color
    val cardColor = if (isSpeaking) ColorTokens.FocusHighlight else baseColor
    val scale = if (isSpeaking) 0.94f else 1f

    val textColor = remember(baseColor) {
        val luminance =
            0.299f * baseColor.red + 0.587f * baseColor.green + 0.114f * baseColor.blue
        if (luminance < 0.5f) Color.White else Color(0xFF1C1B1F)
    }

    val requestSizePx = with(LocalDensity.current) {
        if (isSmall) 96.dp.roundToPx() else 156.dp.roundToPx()
    }

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

    val isIcon = resolvedModel == null
    val vectorPainter = rememberVectorPainter(image = symbol.category.icon)

    val textLayoutResult = remember(symbol.id, symbol.label, isSmall, textColor) {
        val fontSize = if (isSmall) 9.sp else 11.sp
        textMeasurer.measure(
            text = symbol.label,
            style = TextStyle(
                color = textColor,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            ),
            constraints = Constraints(maxWidth = Int.MAX_VALUE),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }

    val imageRequest = remember(symbol.id, resolvedModel, requestSizePx, isIcon) {
        if (isIcon) null
        else {
            ImageRequest.Builder(context)
                .data(resolvedModel)
                .size(requestSizePx)
                .precision(coil.size.Precision.INEXACT)
                .crossfade(false)
                .allowHardware(true)
                .memoryCacheKey("symbol_${symbol.id}_$requestSizePx")
                .build()
        }
    }
    val bitmapPainter = rememberAsyncImagePainter(model = imageRequest)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.82f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .drawWithCache {
                val cornerRadius = CornerRadius(if (isSmall) 30f else 42f)
                val lightGradient = Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.12f), Color.Transparent),
                    startY = 0f,
                    endY = size.height * 0.4f,
                )

                val imgSize = size.height * 0.62f
                val xPos = (size.width - imgSize) / 2f
                val yPos = size.height * 0.12f

                val textX = (size.width - textLayoutResult.size.width) / 2f
                val textY = size.height * 0.78f

                onDrawWithContent {
                    drawRoundRect(color = cardColor, cornerRadius = cornerRadius)
                    drawRoundRect(
                        brush = lightGradient,
                        cornerRadius = cornerRadius,
                        blendMode = BlendMode.Screen,
                    )

                    val painter = if (isIcon) vectorPainter else bitmapPainter
                    translate(left = xPos + parallaxOffset, top = yPos) {
                        with(painter) {
                            draw(
                                size = Size(imgSize, imgSize),
                                alpha = if (isIcon) 0.55f else 1f,
                            )
                        }
                    }

                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(x = textX, y = textY),
                    )
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                if (vibrationEnabled) {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                }
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {}
}
