package br.com.falacomigo.feature.communication

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.falacomigo.core.designsystem.components.SymbolCard
import br.com.falacomigo.core.designsystem.tokens.ColorTokens
import br.com.falacomigo.core.model.SymbolUiModel
import br.com.falacomigo.core.model.Routine
import br.com.falacomigo.core.model.FavoritePhrase
import br.com.falacomigo.core.seed.SeedBoards
import br.com.falacomigo.core.seed.SeedSymbols

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunicationScreen(
    onNavigateToEmergency: () -> Unit,
    onNavigateToBoardSelector: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: CommunicationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val board = state.currentBoard
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = remember { listOf("Início", "Rotinas", "Favoritos") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = board.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = ColorTokens.OnSurface
                        )
                        if (state.isSpeaking) {
                            Spacer(modifier = Modifier.width(12.dp))
                            Surface(color = ColorTokens.FocusHighlight, shape = RoundedCornerShape(12.dp)) {
                                Text("Falando...", fontSize = 10.sp, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                            }
                        }
                    }
                },
                navigationIcon = {
                    if (board.id != "comunicacao") {
                        IconButton(onClick = { viewModel.selectBoard("comunicacao") }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToEmergency) {
                        Icon(Icons.Default.Warning, contentDescription = "Urgente", tint = ColorTokens.Error, modifier = Modifier.size(28.dp))
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Configurações", tint = ColorTokens.OnSurfaceVariant, modifier = Modifier.size(24.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ColorTokens.Surface)
            )
        },
        containerColor = ColorTokens.Background
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = ColorTokens.Surface,
                contentColor = ColorTokens.Primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontSize = 14.sp, fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal) }
                    )
                }
            }

            if (selectedTab == 0) {
                BoardSelectorRow(currentBoardId = board.id, onBoardSelect = { viewModel.selectBoard(it) })
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (selectedTab) {
                    0 -> inicioTab(board, state.imageIdCache, state.speakingSymbolId, state.vibrationEnabled, viewModel)
                    1 -> rotinasTab(viewModel)
                    2 -> favoritosTab(state.imageIdCache, viewModel)
                }
            }
        }
    }
}

@Composable
fun BoardSelectorRow(currentBoardId: String, onBoardSelect: (String) -> Unit) {
    val scrollState = rememberScrollState()
    val selectableBoards = remember { SeedBoards.boards.filter { !it.isEmergency } }
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState).padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        selectableBoards.forEach { board ->
            val isSelected = currentBoardId == board.id
            Surface(
                onClick = { onBoardSelect(board.id) },
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) ColorTokens.Primary else ColorTokens.SurfaceVariant,
                modifier = Modifier.height(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(board.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else ColorTokens.OnSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun inicioTab(
    board: br.com.falacomigo.core.model.BoardUiModel, 
    imageIdCache: Map<String, Int>,
    speakingSymbolId: String?,
    vibrationEnabled: Boolean,
    viewModel: CommunicationViewModel
) {
    val symbols = remember(board.id) { board.symbols }
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items = symbols, key = { it.id }, contentType = { "symbol" }) { symbol ->
            val onSymbolClick = remember(symbol.id) { { viewModel.onSymbolClick(symbol) } }
            SymbolCard(
                symbol = symbol,
                imageResId = imageIdCache[symbol.id.lowercase()] ?: 0,
                isSpeaking = speakingSymbolId == symbol.id,
                vibrationEnabled = vibrationEnabled,
                onClick = onSymbolClick
            )
        }
    }
}

@Composable
private fun rotinasTab(viewModel: CommunicationViewModel) {
    val state by viewModel.state.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            state.routines.forEach { routine ->
                RoutineCard(routine = routine, onClick = { viewModel.playRoutine(routine) }, onDelete = { viewModel.deleteRoutine(routine.id) }, onEdit = { viewModel.startEditRoutine(routine) })
            }
        }
        FloatingActionButton(onClick = { showCreateDialog = true }, modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp), containerColor = ColorTokens.Primary) {
            Icon(Icons.Default.Add, contentDescription = "Nova Rotina", tint = Color.White)
        }
    }

    if (showCreateDialog) {
        RoutineDialog(title = "Criar Rotina", onDismiss = { showCreateDialog = false }, onConfirm = { name, symbols ->
            viewModel.createRoutine(name, symbols.split(" ").filter { it.isNotBlank() })
            showCreateDialog = false
        })
    }

    state.editingRoutine?.let { routine ->
        RoutineDialog(title = "Editar Rotina", initialName = routine.name, initialPhrase = routine.symbols.joinToString(" "), onDismiss = { viewModel.clearEditRoutine() }, onConfirm = { name, symbols ->
            viewModel.updateRoutine(routine.id, name, symbols.split(" ").filter { it.isNotBlank() })
            viewModel.clearEditRoutine()
        })
    }
}

@Composable
private fun RoutineDialog(title: String, initialName: String = "", initialPhrase: String = "", onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var name by remember { mutableStateOf(initialName) }
    var phrase by remember { mutableStateOf(initialPhrase) }
    val suggestedSymbols = remember { SeedSymbols.symbols }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = phrase, onValueChange = { phrase = it }, label = { Text("Frase") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Text("Sugestões:", style = MaterialTheme.typography.labelSmall)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(suggestedSymbols) { symbol ->
                        AssistChip(onClick = { phrase = if (phrase.isBlank()) symbol.label else "$phrase ${symbol.label}" }, label = { Text(symbol.label) })
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onConfirm(name, phrase) }, enabled = name.isNotBlank() && phrase.isNotBlank()) { Text("Salvar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun favoritosTab(imageIdCache: Map<String, Int>, viewModel: CommunicationViewModel) {
    val state by viewModel.state.collectAsState()
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        state.favorites.forEach { fav ->
            val symbol = remember(fav.id) { br.com.falacomigo.core.model.SymbolUiModel(id = fav.id, label = fav.text, spokenText = fav.text, category = "social") }
            val onSymbolClick = remember(fav.id) { { viewModel.onSymbolClick(symbol) } }
            FavoriteCard(favorite = fav, onClick = onSymbolClick)
        }
    }
}

@Composable
private fun RoutineCard(routine: Routine, onClick: () -> Unit, onDelete: () -> Unit, onEdit: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = ColorTokens.SecondaryContainer, tonalElevation = 2.dp) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Schedule, contentDescription = null, tint = ColorTokens.Secondary)
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(routine.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(routine.symbols.joinToString(" "), style = MaterialTheme.typography.bodySmall, color = ColorTokens.OnSecondaryContainer.copy(alpha = 0.7f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Editar", tint = ColorTokens.Primary) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = ColorTokens.Error) }
        }
    }
}

@Composable
private fun FavoriteCard(favorite: FavoritePhrase, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = ColorTokens.PrimaryContainer, tonalElevation = 1.dp) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ColorTokens.Primary)
            Text(favorite.text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp))
        }
    }
}