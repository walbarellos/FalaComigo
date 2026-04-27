package br.com.falacomigo.feature.communication

import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.falacomigo.core.designsystem.components.SymbolCard
import br.com.falacomigo.core.designsystem.tokens.ColorTokens
import br.com.falacomigo.core.model.FavoritePhrase
import br.com.falacomigo.core.model.RoutineUiModel
import br.com.falacomigo.core.model.SymbolUiModel
import kotlin.math.roundToInt

private enum class CommunicationTab(val label: String, val icon: ImageVector) {
    INICIO("Início", Icons.Default.GridView),
    ROTINAS("Gestão", Icons.Default.LibraryBooks),
    FAVORITOS("Favoritos", Icons.Default.Favorite)
}

private val ROOT_FILTER_IDS = setOf("comunicacao", "recentes", "numeral", "social", "alimentacao", "atividades", "necessidades", "emocoes")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunicationScreen(
    onNavigateToEmergency: () -> Unit,
    onNavigateToBoardSelector: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: CommunicationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val currentBoard = state.currentBoard
    val isRoutineBoard = currentBoard.id.startsWith("routine_")

    var selectedTab by remember { mutableStateOf(CommunicationTab.INICIO) }
    var isEditMode by remember { mutableStateOf(false) }

    BackHandler(enabled = isEditMode || currentBoard.id != "comunicacao" || selectedTab != CommunicationTab.INICIO) {
        if (isEditMode) isEditMode = false
        else if (selectedTab != CommunicationTab.INICIO) selectedTab = CommunicationTab.INICIO
        else viewModel.selectBoard("comunicacao")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Organizar" else if (selectedTab == CommunicationTab.ROTINAS) "Gestão de Conteúdo" else currentBoard.title, fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    if (isEditMode) {
                        IconButton(onClick = { isEditMode = false }) { Icon(Icons.Default.Close, "Sair") }
                    } else if (currentBoard.id != "comunicacao") {
                        IconButton(onClick = { viewModel.selectBoard("comunicacao") }) {
                            Icon(Icons.Default.ArrowBack, "Voltar")
                        }
                    }
                },
                actions = {
                    if (!isEditMode && selectedTab == CommunicationTab.INICIO) {
                        IconButton(onClick = { isEditMode = true }) { Icon(Icons.Default.OpenWith, "Mover", tint = ColorTokens.Primary) }
                    }
                    IconButton(onClick = onNavigateToEmergency) { Icon(Icons.Default.Warning, "Urgente", tint = ColorTokens.Error) }
                    IconButton(onClick = onNavigateToSettings) { Icon(Icons.Default.Settings, "Ajustes") }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = ColorTokens.Surface) {
                CommunicationTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        label = { Text(tab.label) },
                        icon = { Icon(tab.icon, null) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (selectedTab == CommunicationTab.INICIO) {
                // ✅ PATCH: Removido o botão "Voltar para Início" duplicado do corpo.
                // Agora apenas a barra de seletores (Chips) aparece quando NÃO é uma rotina.
                if (!isRoutineBoard) {
                    BoardSelectorRow(currentBoardId = currentBoard.id, onBoardSelect = viewModel::selectBoard)
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    CommunicationTab.INICIO -> InicioTab(currentBoard.symbols, state.speakingSymbolId, state.vibrationEnabled, isEditMode, viewModel::onSymbolClick, viewModel::moveSymbol)
                    CommunicationTab.ROTINAS -> RotinasTab(state, viewModel)
                    CommunicationTab.FAVORITOS -> FavoritosTab(state.favorites, viewModel::onSymbolClick)
                }
            }
        }
    }
}

@Composable
private fun BoardSelectorRow(currentBoardId: String, onBoardSelect: (String) -> Unit) {
    val chips = listOf("comunicacao" to "Todos", "recentes" to "Recentes", "numeral" to "Números", "social" to "Social", "alimentacao" to "Comer", "atividades" to "Lazer", "necessidades" to "Preciso", "emocoes" to "Sentir")
    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        chips.forEach { (id, label) ->
            FilterChip(
                selected = currentBoardId == id, 
                onClick = { onBoardSelect(id) }, 
                label = { Text(label) },
                leadingIcon = { if (id == "recentes") Icon(Icons.Default.History, null, modifier = Modifier.size(16.dp)) }
            )
        }
    }
}

@Composable
private fun InicioTab(
    symbols: List<SymbolUiModel>, 
    speakingId: String?, 
    vibration: Boolean, 
    isEditMode: Boolean,
    onClick: (SymbolUiModel) -> Unit,
    onMove: (Int, Int) -> Unit
) {
    val view = LocalView.current
    val gridState = rememberLazyGridState()
    
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(isEditMode) {
                if (!isEditMode) return@pointerInput
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        val item = gridState.layoutInfo.visibleItemsInfo.find { 
                            offset.x.roundToInt() in it.offset.x..(it.offset.x + it.size.width) &&
                            offset.y.roundToInt() in it.offset.y..(it.offset.y + it.size.height)
                        }
                        item?.let {
                            draggedIndex = it.index
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        }
                    },
                    onDragEnd = { draggedIndex = null; dragOffset = Offset.Zero },
                    onDragCancel = { draggedIndex = null; dragOffset = Offset.Zero },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffset += dragAmount
                        val currentDraggedIndex = draggedIndex ?: return@detectDragGesturesAfterLongPress
                        val layoutInfo = gridState.layoutInfo
                        val visibleItems = layoutInfo.visibleItemsInfo
                        val draggedItem = visibleItems.find { it.index == currentDraggedIndex } ?: return@detectDragGesturesAfterLongPress
                        val centerX = draggedItem.offset.x + (draggedItem.size.width / 2) + dragOffset.x
                        val centerY = draggedItem.offset.y + (draggedItem.size.height / 2) + dragOffset.y
                        val targetItem = visibleItems.find { item ->
                            val left = item.offset.x.toFloat()
                            val right = left + item.size.width
                            val top = item.offset.y.toFloat()
                            val bottom = top + item.size.height
                            centerX in left..right && centerY in top..bottom && item.index != currentDraggedIndex
                        }
                        if (targetItem != null) {
                            onMove(currentDraggedIndex, targetItem.index)
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            draggedIndex = targetItem.index
                            dragOffset = Offset.Zero 
                        }
                    }
                )
            }
    ) {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(4), 
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp), 
            horizontalArrangement = Arrangement.spacedBy(10.dp), 
            verticalArrangement = Arrangement.spacedBy(10.dp),
            userScrollEnabled = !isEditMode
        ) {
            itemsIndexed(items = symbols, key = { _, s -> s.id }) { index, symbol ->
                val isSpeaking by remember(speakingId) { derivedStateOf { speakingId == symbol.id } }
                val isBeingDragged = draggedIndex == index

                Box(
                    modifier = Modifier
                        .zIndex(if (isBeingDragged) 10f else 0f)
                        .graphicsLayer {
                            if (isBeingDragged) {
                                translationX = dragOffset.x
                                translationY = dragOffset.y
                                scaleX = 1.15f
                                scaleY = 1.15f
                                shadowElevation = 30f
                                alpha = 0.9f
                            } else {
                                translationX = 0f
                                translationY = 0f
                                scaleX = 1f
                                scaleY = 1f
                                alpha = if (isEditMode) 0.6f else 1f
                            }
                        }
                ) {
                    SymbolCard(symbol = symbol, isSpeaking = isSpeaking, vibrationEnabled = vibration, onClick = { if (!isEditMode) onClick(symbol) })
                    if (isEditMode) {
                        Icon(Icons.Default.DragHandle, null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun RotinasTab(state: CommunicationState, viewModel: CommunicationViewModel) {
    var showCreateRoutine by remember { mutableStateOf(false) }
    var showWordCreator by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Text("Grupos de Acesso Rápido", style = MaterialTheme.typography.titleSmall, color = ColorTokens.Primary, fontWeight = FontWeight.Bold) }
            items(state.routines, key = { it.id }) { routine ->
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = ColorTokens.SurfaceVariant, onClick = { viewModel.openRoutineAsBoard(routine) }) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(routine.title, fontWeight = FontWeight.Bold)
                            Text("${routine.symbols.size} itens", fontSize = 12.sp)
                        }
                        IconButton(onClick = { viewModel.startEditRoutine(routine) }) { Icon(Icons.Default.Edit, null) }
                        IconButton(onClick = { viewModel.deleteRoutine(routine.id) }) { Icon(Icons.Default.Delete, null, tint = ColorTokens.Error) }
                    }
                }
            }
        }
        Column(modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ExtendedFloatingActionButton(onClick = { showWordCreator = true }, icon = { Icon(Icons.Default.Add, null) }, text = { Text("Nova Palavra") }, containerColor = ColorTokens.Secondary, contentColor = Color.White)
            ExtendedFloatingActionButton(onClick = { showCreateRoutine = true }, icon = { Icon(Icons.Default.LibraryAdd, null) }, text = { Text("Nova Rotina") }, containerColor = ColorTokens.Primary, contentColor = Color.White)
        }
    }
    if (showCreateRoutine || state.editingRoutine != null) {
        RoutineManagerDialog(title = if (state.editingRoutine == null) "Nova Rotina" else "Editar Rotina", routine = state.editingRoutine, initialSymbols = state.editingRoutineSymbols, searchResults = state.searchResults, isSearching = state.isSearching, onDismiss = { if (state.editingRoutine != null) viewModel.clearEditRoutine() else showCreateRoutine = false }, onSave = { n, s -> viewModel.saveRoutine(n, s); showCreateRoutine = false }, onSearch = viewModel::onSearchQueryChanged, onSymbolClick = viewModel::onSymbolClick)
    }
    if (showWordCreator) {
        WordCreatorDialog(onDismiss = { showWordCreator = false }, onWordCreated = { viewModel.saveSymbol(it); showWordCreator = false }, onSearch = viewModel::onSearchQueryChanged, searchResults = state.searchResults, isSearching = state.isSearching)
    }
}

@Composable
private fun RoutineManagerDialog(title: String, routine: RoutineUiModel?, initialSymbols: List<SymbolUiModel>, searchResults: List<SymbolUiModel>, isSearching: Boolean, onDismiss: () -> Unit, onSave: (String, List<SymbolUiModel>) -> Unit, onSearch: (String) -> Unit, onSymbolClick: (SymbolUiModel) -> Unit) {
    var name by remember(routine) { mutableStateOf(routine?.title ?: "") }
    var selectedSymbols by remember(initialSymbols) { mutableStateOf(initialSymbols) }
    var query by remember { mutableStateOf("") }
    var showWordCreatorInRoutine by remember { mutableStateOf(false) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title, fontWeight = FontWeight.ExtraBold) }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome") }, modifier = Modifier.fillMaxWidth())
            HorizontalDivider()
            Text("Itens Selecionados:", style = MaterialTheme.typography.labelLarge, color = ColorTokens.Primary)
            if (selectedSymbols.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.height(70.dp)) {
                    items(selectedSymbols, key = { it.id }) { s ->
                        Box(modifier = Modifier.size(60.dp)) { SymbolCard(symbol = s, isSmall = true, onClick = { selectedSymbols = selectedSymbols - s }) }
                    }
                }
            } else {
                Text("Adicione palavras abaixo.", fontSize = 11.sp, color = Color.Gray)
            }
            OutlinedTextField(value = query, onValueChange = { query = it; onSearch(it) }, placeholder = { Text("Buscar no catálogo...") }, modifier = Modifier.fillMaxWidth(), trailingIcon = { if (isSearching) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else Icon(Icons.Default.Search, null) })
            if (searchResults.isNotEmpty()) {
                Text("Resultados:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.height(80.dp)) {
                    items(searchResults, key = { it.id }) { s ->
                        Box(modifier = Modifier.size(70.dp)) { SymbolCard(symbol = s, isSmall = true, onClick = { if (!selectedSymbols.any { it.id == s.id }) selectedSymbols = selectedSymbols + s }) }
                    }
                }
            }
            Button(onClick = { showWordCreatorInRoutine = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = ColorTokens.Secondary)) {
                Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Criar Nova Palavra")
            }
        }
    }, confirmButton = { Button(onClick = { onSave(name, selectedSymbols) }, enabled = name.isNotBlank() && selectedSymbols.isNotEmpty()) { Text("Salvar") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } })
    if (showWordCreatorInRoutine) WordCreatorDialog(onDismiss = { showWordCreatorInRoutine = false }, onWordCreated = { selectedSymbols = selectedSymbols + it; showWordCreatorInRoutine = false }, onSearch = onSearch, searchResults = searchResults, isSearching = isSearching)
}

@Composable
private fun WordCreatorDialog(onDismiss: () -> Unit, onWordCreated: (SymbolUiModel) -> Unit, onSearch: (String) -> Unit, searchResults: List<SymbolUiModel>, isSearching: Boolean) {
    var label by remember { mutableStateOf("") }
    var talk by remember { mutableStateOf("") }
    var selectedImg by remember { mutableStateOf<SymbolUiModel?>(null) }
    var query by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Nova Palavra", fontWeight = FontWeight.Bold) }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = label, onValueChange = { label = it; if(talk.isEmpty()) talk = it }, label = { Text("Nome") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = talk, onValueChange = { talk = it }, label = { Text("Voz") }, modifier = Modifier.fillMaxWidth())
            Text("Imagem:", style = MaterialTheme.typography.labelMedium)
            OutlinedTextField(value = query, onValueChange = { query = it; onSearch(it) }, placeholder = { Text("Pesquisar...") }, modifier = Modifier.fillMaxWidth(), trailingIcon = { if (isSearching) CircularProgressIndicator(modifier = Modifier.size(20.dp)) })
            if (selectedImg != null) { Box(modifier = Modifier.size(60.dp).align(Alignment.CenterHorizontally)) { SymbolCard(symbol = selectedImg!!, isSmall = true, onClick = {}) } }
            if (searchResults.isNotEmpty()) {
                LazyRow(modifier = Modifier.height(80.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(searchResults, key = { it.id }) { s ->
                        Box(modifier = Modifier.size(70.dp)) { SymbolCard(symbol = s, isSmall = true, onClick = { selectedImg = s }) }
                    }
                }
            }
        }
    }, confirmButton = { Button(onClick = { val finalWord = selectedImg?.copy(id = "custom_${System.currentTimeMillis()}", label = label, spokenText = talk, isCustom = true); if (finalWord != null) onWordCreated(finalWord) }, enabled = label.isNotBlank() && selectedImg != null) { Text("Criar") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } })
}

@Composable
private fun FavoritosTab(favorites: List<FavoritePhrase>, onSymbolClick: (SymbolUiModel) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(favorites, key = { it.id }) { fav ->
            Surface(onClick = { onSymbolClick(SymbolUiModel(id = fav.id, label = fav.text, spokenText = fav.text)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = ColorTokens.PrimaryContainer) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Default.AutoAwesome, null, tint = ColorTokens.Primary)
                    Text(fav.text, modifier = Modifier.padding(start = 12.dp), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}