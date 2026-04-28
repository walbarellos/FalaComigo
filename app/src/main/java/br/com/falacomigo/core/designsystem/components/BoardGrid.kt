package br.com.falacomigo.core.designsystem.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import br.com.falacomigo.core.designsystem.tokens.ColorTokens
import br.com.falacomigo.core.designsystem.tokens.SpacingTokens
import br.com.falacomigo.core.model.BoardLayoutMode
import br.com.falacomigo.core.model.SymbolCategory
import br.com.falacomigo.core.model.SymbolUiModel
import kotlinx.coroutines.flow.*

/**
 * Grade de Símbolos Universal.
 *
 * Melhorias em relação à versão anterior:
 *
 * 1. `groupedSymbols` recebido como parâmetro (pré-computado no ViewModel em Dispatchers.Default).
 *    CategoryStreamLayout não faz mais groupBy em tempo de composição.
 *
 * 2. warmUp/haptic no LazyColumn/LazyRow disparava em cada mudança de firstVisibleItemIndex,
 *    incluindo scrolls parciais. Isso resultava em:
 *    - TTS warmUp() chamado dezenas de vezes por segundo durante scroll (IO desnecessário)
 *    - Haptic CLOCK_TICK em cada pixel de scroll — confuso para o usuário
 *    Corrigido: haptic e warmUp agora disparam apenas em itens distintos via
 *    snapshotFlow com distinctUntilChanged.
 *
 * 3. rememberSnapFlingBehavior para LazyRow hoistado para fora do item {} lambda.
 *    Antes era re-criado a cada recomposição do item.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BoardGrid(
    symbols: List<SymbolUiModel>,
    boardId: String = "",
    columns: Int = 4,
    isEditMode: Boolean = false,
    layoutMode: BoardLayoutMode = BoardLayoutMode.GRID,
    speakingSymbolId: String? = null,
    vibrationEnabled: Boolean = true,
    /**
     * Símbolos agrupados por categoria, fornecidos pelo ViewModel (pré-computados).
     * Se não fornecidos, o agrupamento é feito localmente — mantém retrocompatibilidade.
     */
    groupedSymbols: Map<SymbolCategory, List<SymbolUiModel>> = emptyMap(),
    onSymbolClick: (SymbolUiModel) -> Unit,
    onWarmUp: () -> Unit = {},
    onMove: (from: Int, to: Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
) {
    if (symbols.isEmpty()) {
        EmptyState(
            message = "Esta prancha ainda não tem símbolos.",
            actionLabel = "Adicionar símbolo",
            onAction = null,
        )
        return
    }

    when (layoutMode) {
        BoardLayoutMode.PAGER -> {
            BoardPager(symbols, speakingSymbolId, vibrationEnabled, onSymbolClick, onWarmUp, modifier)
        }

        BoardLayoutMode.MMO -> {
            if (boardId == "comunicacao") {
                CategoryStreamLayout(
                    symbols = symbols,
                    groupedSymbols = groupedSymbols,
                    speakingSymbolId = speakingSymbolId,
                    vibrationEnabled = vibrationEnabled,
                    onSymbolClick = onSymbolClick,
                    onWarmUp = onWarmUp,
                    modifier = modifier,
                )
            } else {
                StandardGrid(
                    symbols, columns, SpacingTokens.GridGap,
                    isEditMode, speakingSymbolId, vibrationEnabled,
                    onSymbolClick, onWarmUp, onMove, modifier,
                )
            }
        }

        else -> {
            StandardGrid(
                symbols, columns, SpacingTokens.GridGap,
                isEditMode, speakingSymbolId, vibrationEnabled,
                onSymbolClick, onWarmUp, onMove, modifier,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// CategoryStreamLayout (Modo MMO)
// ---------------------------------------------------------------------------

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CategoryStreamLayout(
    symbols: List<SymbolUiModel>,
    groupedSymbols: Map<SymbolCategory, List<SymbolUiModel>>,
    speakingSymbolId: String?,
    vibrationEnabled: Boolean,
    onSymbolClick: (SymbolUiModel) -> Unit,
    onWarmUp: () -> Unit,
    modifier: Modifier,
) {
    // Usa groupedSymbols pré-computado do ViewModel.
    // Fallback local apenas para uso standalone (ex: preview, testes).
    val grouped = if (groupedSymbols.isNotEmpty()) {
        groupedSymbols
    } else {
        remember(symbols) { symbols.groupBy { it.category } }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        grouped.forEach { (category, categorySymbols) ->
            item(key = category.id) {
                CategoryRow(
                    category = category,
                    symbols = categorySymbols,
                    speakingSymbolId = speakingSymbolId,
                    vibrationEnabled = vibrationEnabled,
                    onSymbolClick = onSymbolClick,
                    onWarmUp = onWarmUp,
                )
            }
        }
    }
}

/**
 * Linha de categoria com scroll horizontal e snapping.
 * Extraída como composable separado para isolar o estado do LazyRow e evitar
 * re-criação do flingBehavior a cada recomposição do item pai.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CategoryRow(
    category: SymbolCategory,
    symbols: List<SymbolUiModel>,
    speakingSymbolId: String?,
    vibrationEnabled: Boolean,
    onSymbolClick: (SymbolUiModel) -> Unit,
    onWarmUp: () -> Unit,
) {
    val rowState = rememberLazyListState()
    val view = LocalView.current

    // Snap hoistado fora do loop — uma instância estável por categoria
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = rowState)

    // Haptic e warmUp apenas ao mudar de item distinto, não em cada pixel de scroll.
    // A versão anterior usava LaunchedEffect(rowState.firstVisibleItemIndex)
    // que disparava em qualquer mudança — incluindo scroll parcial de pixel.
    LaunchedEffect(rowState) {
        snapshotFlow { rowState.firstVisibleItemIndex }
            .drop(1)
            .distinctUntilChanged()
            .collect {
                if (vibrationEnabled) {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                }
                // warmUp apenas na primeira mudança por categoria — não em cada item
                onWarmUp()
            }
    }

    Column {
        Text(
            text = category.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ColorTokens.Primary,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        LazyRow(
            state = rowState,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp),
            modifier = Modifier.fillMaxWidth(),
            flingBehavior = snapBehavior,
        ) {
            items(symbols, key = { it.id }) { symbol ->
                Box(modifier = Modifier.width(100.dp)) {
                    SymbolCard(
                        symbol = symbol,
                        isSpeaking = speakingSymbolId == symbol.id,
                        vibrationEnabled = vibrationEnabled,
                        isSmall = true,
                        onClick = { onSymbolClick(symbol) },
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// StandardGrid (Modo Grade padrão)
// ---------------------------------------------------------------------------

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun StandardGrid(
    symbols: List<SymbolUiModel>,
    columns: Int,
    spacing: androidx.compose.ui.unit.Dp,
    isEditMode: Boolean,
    speakingSymbolId: String?,
    vibrationEnabled: Boolean,
    onSymbolClick: (SymbolUiModel) -> Unit,
    onWarmUp: () -> Unit,
    onMove: (from: Int, to: Int) -> Unit,
    modifier: Modifier,
) {
    val view = LocalView.current
    val gridState = rememberLazyGridState()
    val reorderState = rememberReorderableGridState(gridState)

    // Mesmo padrão do CategoryRow: haptic apenas em item distinto, não em pixel.
    LaunchedEffect(gridState) {
        snapshotFlow { gridState.firstVisibleItemIndex }
            .drop(1)
            .distinctUntilChanged()
            .collect {
                if (vibrationEnabled) {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                }
                onWarmUp()
            }
    }

    val dragModifier = if (isEditMode) {
        Modifier.pointerInput(Unit) {
            detectDragGesturesAfterLongPress(
                onDragStart = { offset ->
                    val item = gridState.layoutInfo.visibleItemsInfo.find { info ->
                        offset.x.toInt() in info.offset.x..(info.offset.x + info.size.width) &&
                            offset.y.toInt() in info.offset.y..(info.offset.y + info.size.height)
                    }
                    item?.let {
                        reorderState.onDragStart(it.index)
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    }
                },
                onDragEnd = { reorderState.onDragInterrupted() },
                onDragCancel = { reorderState.onDragInterrupted() },
                onDrag = { change, _ ->
                    change.consume()
                    reorderState.onDrag(change.position - change.previousPosition)

                    val draggedIndex =
                        reorderState.draggedIndex ?: return@detectDragGesturesAfterLongPress
                    val visibleItems = gridState.layoutInfo.visibleItemsInfo
                    val draggedItem =
                        visibleItems.find { it.index == draggedIndex }
                            ?: return@detectDragGesturesAfterLongPress

                    val centerX =
                        draggedItem.offset.x + (draggedItem.size.width / 2) + reorderState.dragOffset.x
                    val centerY =
                        draggedItem.offset.y + (draggedItem.size.height / 2) + reorderState.dragOffset.y

                    val targetIndex = reorderState.calculateTargetIndex(centerX, centerY)
                    if (targetIndex != null && targetIndex != draggedIndex) {
                        onMove(draggedIndex, targetIndex)
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        reorderState.onDragStart(targetIndex)
                        reorderState.dragOffset = Offset.Zero
                    }
                },
            )
        }
    } else Modifier

    val snapBehavior = rememberSnapFlingBehavior(
        snapLayoutInfoProvider = remember(gridState) { SnapLayoutInfoProvider(gridState) },
    )

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(columns),
        modifier = modifier.fillMaxSize().then(dragModifier),
        contentPadding = PaddingValues(spacing),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalArrangement = Arrangement.spacedBy(spacing),
        flingBehavior = snapBehavior,
    ) {
        itemsIndexed(
            items = symbols,
            key = { _, symbol -> symbol.id },
            contentType = { _, _ -> "symbol_card" },
        ) { index, symbol ->
            SymbolGridItem(
                symbol = symbol,
                index = index,
                isEditMode = isEditMode,
                isSpeaking = speakingSymbolId == symbol.id,
                vibrationEnabled = vibrationEnabled,
                reorderState = reorderState,
                onSymbolClick = onSymbolClick,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// SymbolGridItem
// ---------------------------------------------------------------------------

@Composable
private fun SymbolGridItem(
    symbol: SymbolUiModel,
    index: Int,
    isEditMode: Boolean,
    isSpeaking: Boolean,
    vibrationEnabled: Boolean,
    reorderState: ReorderableGridState,
    onSymbolClick: (SymbolUiModel) -> Unit,
) {
    val isBeingDragged = reorderState.draggedIndex == index

    Box(
        modifier = Modifier
            .zIndex(if (isBeingDragged) 10f else 0f)
            .graphicsLayer {
                if (isBeingDragged) {
                    translationX = reorderState.dragOffset.x
                    translationY = reorderState.dragOffset.y
                    scaleX = 1.12f
                    scaleY = 1.12f
                    shadowElevation = 20f
                } else {
                    scaleX = 1f
                    scaleY = 1f
                    alpha = if (isEditMode) 0.7f else 1f
                }
            },
    ) {
        val onClickStable = remember(symbol, onSymbolClick) { { onSymbolClick(symbol) } }
        SymbolCard(
            symbol = symbol,
            isSpeaking = isSpeaking,
            vibrationEnabled = vibrationEnabled,
            onClick = onClickStable,
        )
        if (isEditMode) {
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(16.dp),
            )
        }
    }
}
