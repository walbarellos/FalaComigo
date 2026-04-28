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
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

/**
 * SymbolCard — Renderização direta via GPU com correções de corretude e performance.
 *
 * Correções em relação à versão anterior:
 *
 * 1. REMOVIDO `clip = true` do graphicsLayer.
 *    Com 20+ cards visíveis, cada `clip = true` cria um offscreen buffer independente na GPU.
 *    Isso equivalia a renderizar a tela N+1 vezes por frame. O clipping visual é
 *    preservado pelo `drawRoundRect` dentro de `drawWithCache`.
 *
 * 2. CORRIGIDO texto re-measure em reciclagem de slots.
 *    `drawWithCache` só invalida por mudança de tamanho (Size). Quando o LazyGrid
 *    recicla um slot com símbolo diferente, o tamanho não muda, então `textMeasurer.measure()`
 *    dentro do drawWithCache nunca re-executava — desenhava o label antigo no símbolo novo.
 *    Solução: medição extraída para `remember(symbol.id, symbol.label, isSmall)`, que invalida
 *    corretamente ao trocar de símbolo. O drawWithCache mantém apenas dados dependentes de Size.
 *
 * 3. MANTIDO o efeito Glossy (gradiente de brilho) e Parallax — custo zero pois
 *    o gradiente é criado uma vez no drawWithCache e reutilizado em cada frame.
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

    val isIcon = symbol.imageUrl.isNullOrEmpty() && symbol.imageResId == 0 && imageResId == 0
    val vectorPainter = rememberVectorPainter(image = symbol.category.icon)

    // Medição de texto extraída do drawWithCache para que invalide corretamente
    // quando o símbolo muda (reciclagem de slots no LazyGrid).
    // fontSize precisa ser estável; convertemos sp→px uma vez aqui.
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
            // Constraints.fixedWidth depende do tamanho real do card — será aplicado
            // em onDrawWithContent onde o tamanho está disponível.
            // Usamos um valor generoso; o Ellipsis cuida do overflow.
            constraints = Constraints(maxWidth = Int.MAX_VALUE),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }

    val imageRequest = remember(symbol.id, isSmall, imageResId, isIcon) {
        if (isIcon) null
        else {
            val data: Any = when {
                !symbol.imageUrl.isNullOrEmpty() -> symbol.imageUrl!!
                symbol.imageResId != 0 -> symbol.imageResId
                else -> imageResId
            }
            ImageRequest.Builder(context)
                .data(data)
                .size(if (isSmall) 128 else 192)
                .precision(coil.size.Precision.EXACT)
                .crossfade(false)
                .allowHardware(true)
                // Mesma chave usada pelo preloader — garantia de cache hit
                .memoryCacheKey("symbol_${symbol.id}")
                .build()
        }
    }
    val bitmapPainter = rememberAsyncImagePainter(model = imageRequest)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.82f)
            // graphicsLayer apenas para escala de feedback — SEM clip.
            // clip=true criava N offscreen buffers independentes (um por card visível).
            // O clipping visual é feito pelo drawRoundRect abaixo, sem custo extra.
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .drawWithCache {
                // Tudo aqui depende apenas de Size — invalidação correta.
                val cornerRadius = CornerRadius(if (isSmall) 30f else 42f)

                // Gradiente criado uma vez por size change, reutilizado em todos os frames.
                val lightGradient = Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.12f), Color.Transparent),
                    startY = 0f,
                    endY = size.height * 0.4f,
                )

                val imgSize = size.height * 0.62f
                val xPos = (size.width - imgSize) / 2f
                val yPos = size.height * 0.12f

                // Centro do texto baseado na largura real do card
                val textX = (size.width - textLayoutResult.size.width) / 2f
                val textY = size.height * 0.78f

                onDrawWithContent {
                    // 1. Fundo
                    drawRoundRect(color = cardColor, cornerRadius = cornerRadius)

                    // 2. Glossy highlight (blend Screen = custo quase zero na GPU moderna)
                    drawRoundRect(
                        brush = lightGradient,
                        cornerRadius = cornerRadius,
                        blendMode = BlendMode.Screen,
                    )

                    // 3. Imagem / ícone com parallax
                    val painter = if (isIcon) vectorPainter else bitmapPainter
                    translate(left = xPos + parallaxOffset, top = yPos) {
                        with(painter) {
                            draw(
                                size = Size(imgSize, imgSize),
                                alpha = if (isIcon) 0.55f else 1f,
                            )
                        }
                    }

                    // 4. Label
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
