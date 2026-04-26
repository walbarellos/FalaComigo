package br.com.falacomigo.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.falacomigo.core.designsystem.tokens.SpacingTokens
import br.com.falacomigo.core.model.SymbolUiModel

@Composable
fun BoardGrid(
    symbols: List<SymbolUiModel>,
    columns: Int = 3,
    onSymbolClick: (SymbolUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    if (symbols.isEmpty()) {
        EmptyState(
            message = "Esta prancha ainda não tem símbolos.",
            actionLabel = "Adicionar símbolo",
            onAction = null
        )
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(SpacingTokens.ScreenPadding),
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.GridGap),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.GridGap)
    ) {
        items(
            items = symbols,
            key = { it.id }
        ) { symbol ->
            SymbolCard(
                symbol = symbol,
                onClick = { onSymbolClick(symbol) }
            )
        }
    }
}