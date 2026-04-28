package br.com.falacomigo.core.designsystem.components

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset

/**
 * Máquina de Estados para a lógica de reordenação (Drag-and-Drop).
 * Garante Encapsulamento e Local Reasoning.
 */
@Stable
class ReorderableGridState(
    val gridState: LazyGridState
) {
    var draggedIndex by mutableStateOf<Int?>(null)
    var dragOffset by mutableStateOf(Offset.Zero)

    fun onDragStart(index: Int) {
        draggedIndex = index
    }

    fun onDrag(offset: Offset) {
        dragOffset += offset
    }

    fun onDragInterrupted() {
        draggedIndex = null
        dragOffset = Offset.Zero
    }

    fun calculateTargetIndex(centerX: Float, centerY: Float): Int? {
        val layoutInfo = gridState.layoutInfo
        val visibleItems = layoutInfo.visibleItemsInfo
        return visibleItems.find { item ->
            val left = item.offset.x.toFloat()
            val right = left + item.size.width
            val top = item.offset.y.toFloat()
            val bottom = top + item.size.height
            centerX in left..right && centerY in top..bottom
        }?.index
    }
}

@Composable
fun rememberReorderableGridState(gridState: LazyGridState): ReorderableGridState {
    return remember(gridState) { ReorderableGridState(gridState) }
}
