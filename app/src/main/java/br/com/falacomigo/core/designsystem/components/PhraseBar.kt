package br.com.falacomigo.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import br.com.falacomigo.core.designsystem.tokens.ColorTokens
import br.com.falacomigo.core.designsystem.tokens.SpacingTokens
import br.com.falacomigo.core.model.SymbolUiModel

@Composable
fun PhraseBar(
    symbols: List<SymbolUiModel>,
    onSpeak: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (symbols.isEmpty()) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ColorTokens.Surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LISTA DE PICTOGRAMAS NA BARRA
            LazyRow(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(symbols) { symbol ->
                    Box(modifier = Modifier.width(60.dp)) {
                        SymbolCard(
                            symbol = symbol,
                            imageResId = 0,
                            isSmall = true,
                            onClick = {} // Sem ação dentro da barra por enquanto
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // BOTÕES DE AÇÃO
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onClear,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.Clear, contentDescription = "Limpar")
                }
                
                IconButton(
                    onClick = onSpeak,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = ColorTokens.Primary,
                        contentColor = ColorTokens.OnPrimary
                    ),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Falar")
                }
            }
        }
    }
}

@Composable
fun PhraseBarCollapsed(
    onExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(ColorTokens.Surface)
            .padding(horizontal = SpacingTokens.Md),
        contentAlignment = Alignment.CenterStart
    ) {
        TextButton(onClick = onExpand) {
            Text("Modo frase")
        }
    }
}