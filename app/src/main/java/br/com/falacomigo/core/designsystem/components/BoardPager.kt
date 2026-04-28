package br.com.falacomigo.core.designsystem.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import br.com.falacomigo.core.model.SymbolUiModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlin.math.absoluteValue

/**
 * Visualização de Foco (Pager) com Orquestração Sensorial.
 * Implementa: Parallax 2.5D, Haptic Sync e Áudio Preditivo.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BoardPager(
    symbols: List<SymbolUiModel>,
    speakingSymbolId: String? = null,
    vibrationEnabled: Boolean = true,
    onSymbolClick: (SymbolUiModel) -> Unit,
    onWarmUp: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { symbols.size })
    val view = LocalView.current

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .drop(1)
            .distinctUntilChanged()
            .collect {
                if (vibrationEnabled) {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                }
                onWarmUp()
            }
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 60.dp),
        pageSpacing = 16.dp,
        verticalAlignment = Alignment.CenterVertically
    ) { page ->
        val symbol = symbols[page]
        
        // Cálculo de deslocamento relativo da página (-1.0 a 1.0)
        val pageOffset = (
            (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
        )

        // ✅ NASA Parallax: Imagem se move 20% da velocidade da página
        // Isso cria a ilusão de profundidade física (2.5D)
        val parallaxX = pageOffset * 22f

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp)
                .graphicsLayer {
                    // Efeito de profundidade no Card
                    val scale = lerp(
                        start = 0.82f,
                        stop = 1f,
                        fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
                    )
                    scaleX = scale
                    scaleY = scale
                    alpha = lerp(
                        start = 0.4f,
                        stop = 1f,
                        fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            SymbolCard(
                symbol = symbol,
                isSpeaking = speakingSymbolId == symbol.id,
                vibrationEnabled = vibrationEnabled,
                isSmall = false,
                parallaxOffset = parallaxX, // INJETADO NO CUSTOM LAYOUT
                onClick = { onSymbolClick(symbol) }
            )
        }
    }
}
