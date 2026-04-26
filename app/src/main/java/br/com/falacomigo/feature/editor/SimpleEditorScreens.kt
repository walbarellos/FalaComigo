package br.com.falacomigo.feature.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.falacomigo.core.designsystem.tokens.ColorTokens
import br.com.falacomigo.core.designsystem.tokens.SpacingTokens
import br.com.falacomigo.core.model.SymbolUiModel
import br.com.falacomigo.core.seed.SeedBoards
import br.com.falacomigo.core.seed.SeedSymbols

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardEditorScreen(
    boardId: String,
    onNavigateBack: () -> Unit,
    onNavigateToSymbolPicker: () -> Unit,
    onNavigateToSlotEditor: (String) -> Unit,
    onNavigateToMoveSymbol: () -> Unit
) {
    val board = SeedBoards.findById(boardId)
    val allSymbols = SeedSymbols.symbols

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(board?.title ?: "Editar Prancha") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ColorTokens.Surface),
                actions = {
                    Text("Mover", modifier = Modifier.padding(end = 8.dp).clickable { onNavigateToMoveSymbol() })
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToSymbolPicker,
                containerColor = ColorTokens.Primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar símbolo")
            }
        }
    ) { paddingValues ->
        if (board == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Prancha não encontrada")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Text(
                text = "Toque em um símbolo para editar",
                style = MaterialTheme.typography.bodyMedium,
                color = ColorTokens.OnSurfaceVariant,
                modifier = Modifier.padding(SpacingTokens.Md)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(SpacingTokens.ScreenPadding),
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.GridGap),
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.GridGap)
            ) {
                items(board.symbols) { symbol ->
                    EditorSymbolCard(
                        symbol = symbol,
                        onClick = { onNavigateToSlotEditor(symbol.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun EditorSymbolCard(
    symbol: SymbolUiModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ColorTokens.SymbolCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ColorTokens.PrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = symbol.label.take(1),
                        style = MaterialTheme.typography.titleLarge,
                        color = ColorTokens.OnPrimaryContainer
                    )
                }
                Text(
                    text = symbol.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = ColorTokens.OnSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymbolPickerScreen(
    boardId: String,
    onNavigateBack: () -> Unit,
    onSymbolSelected: (String) -> Unit
) {
    val availableSymbols = SeedSymbols.symbols

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escolher Símbolo") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ColorTokens.Surface)
            )
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(SpacingTokens.ScreenPadding),
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.GridGap),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.GridGap)
        ) {
            items(availableSymbols) { symbol ->
                Card(
                    modifier = Modifier
                        .size(100.dp)
                        .clickable { onSymbolSelected(symbol.id) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ColorTokens.SymbolCardBackground)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ColorTokens.PrimaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    symbol.label.take(1),
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                            Text(
                                symbol.label,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotEditorScreen(
    boardId: String,
    symbolId: String,
    onNavigateBack: () -> Unit
) {
    val symbol = SeedSymbols.findById(symbolId)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Símbolo") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ColorTokens.Surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(SpacingTokens.Md)
        ) {
            if (symbol != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(SpacingTokens.Md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ColorTokens.PrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                symbol.label.take(1),
                                style = MaterialTheme.typography.headlineLarge
                            )
                        }
                        Column(modifier = Modifier.padding(start = SpacingTokens.Md)) {
                            Text("Label: ${symbol.label}", style = MaterialTheme.typography.titleMedium)
                            Text("Fala: ${symbol.spokenText}", style = MaterialTheme.typography.bodyMedium)
                            Text("Categoria: ${symbol.category}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Text(
                    text = "Este é um símbolo seed. Para editar, crie um símbolo personalizado.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ColorTokens.OnSurfaceVariant,
                    modifier = Modifier.padding(top = SpacingTokens.Md)
                )
            } else {
                Text("Símbolo não encontrado")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoveSymbolScreen(
    boardId: String,
    onNavigateBack: () -> Unit
) {
    val board = SeedBoards.findById(boardId)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mover Símbolos") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ColorTokens.Surface)
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(SpacingTokens.Md)) {
            Text("Arraste os símbolos para reordenar", style = MaterialTheme.typography.bodyLarge)
            Text("Ou clique em cima/baixo para mover", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                modifier = Modifier.padding(top = SpacingTokens.Md)
            ) {
                items(board?.symbols ?: emptyList()) { symbol ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = SpacingTokens.Xs),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(SpacingTokens.Md).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(symbol.label)
                            Text("↑↓", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinGateScreen(
    onNavigateBack: () -> Unit,
    onEditorVerified: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Área do Cuidador") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ColorTokens.Surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(SpacingTokens.Xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Área para adulto ou cuidador",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Para editar pranchas e símbolos.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = SpacingTokens.Md)
            )
            Text(
                text = "Esta área é protegida para evitar que crianças/editem sem querer.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = ColorTokens.OnSurfaceVariant,
                modifier = Modifier.padding(top = SpacingTokens.Lg)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = SpacingTokens.Xxl)
                    .clickable { onEditorVerified("principal") },
                colors = CardDefaults.cardColors(containerColor = ColorTokens.Primary)
            ) {
                Text(
                    text = "Entrar no Editor",
                    style = MaterialTheme.typography.titleMedium,
                    color = ColorTokens.OnPrimary,
                    modifier = Modifier.padding(SpacingTokens.Lg).fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}