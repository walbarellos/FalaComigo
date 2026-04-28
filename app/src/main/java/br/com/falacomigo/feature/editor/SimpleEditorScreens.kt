package br.com.falacomigo.feature.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.falacomigo.core.designsystem.tokens.ColorTokens
import br.com.falacomigo.core.designsystem.tokens.SpacingTokens
import br.com.falacomigo.core.model.SymbolCategory
import br.com.falacomigo.core.model.SymbolUiModel
import br.com.falacomigo.core.model.resolveImageModel
import br.com.falacomigo.core.seed.SeedSymbols
import br.com.falacomigo.data.remote.ArasaacPictogram
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardEditorScreen(
    boardId: String,
    onNavigateBack: () -> Unit,
    onNavigateToSymbolPicker: () -> Unit,
    onNavigateToSlotEditor: (String) -> Unit,
    onNavigateToMoveSymbol: () -> Unit,
    viewModel: BoardEditorViewModel = hiltViewModel()
) {
    val boardState by viewModel.board.collectAsState()

    LaunchedEffect(boardId) {
        viewModel.loadBoard(boardId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(boardState?.title ?: "Carregando...") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ColorTokens.Surface),
                actions = {
                    if (boardState != null) {
                        TextButton(onClick = onNavigateToMoveSymbol) {
                            Text("Reordenar", color = ColorTokens.Primary)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (boardState != null) {
                FloatingActionButton(
                    onClick = onNavigateToSymbolPicker,
                    containerColor = ColorTokens.Primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar símbolo")
                }
            }
        }
    ) { paddingValues ->
        var isLoading by remember { mutableStateOf(true) }
        
        LaunchedEffect(boardState) {
            if (boardState != null) {
                isLoading = false
            } else {
                delay(1500)
                if (boardState == null) isLoading = false
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (boardState == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ErrorOutline, null, modifier = Modifier.size(48.dp), tint = ColorTokens.Error)
                    Spacer(Modifier.height(8.dp))
                    Text("Prancha não encontrada no banco.")
                    TextButton(onClick = onNavigateBack) { Text("Voltar") }
                }
            }
        } else {
            val board = boardState!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Text(
                    text = "Toque em um símbolo para editar nome, voz ou imagem",
                    style = MaterialTheme.typography.labelMedium,
                    color = ColorTokens.OnSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
}

@Composable
fun EditorSymbolCard(
    symbol: SymbolUiModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ColorTokens.SymbolCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            val model = symbol.resolveImageModel(LocalContext.current)
            
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                if (model != null) {
                    AsyncImage(
                        model = model,
                        contentDescription = symbol.label,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Icon(Icons.Default.Image, null, modifier = Modifier.size(32.dp), tint = ColorTokens.OnSurfaceVariant.copy(alpha = 0.5f))
                }
            }
            Text(
                text = symbol.label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymbolPickerScreen(
    boardId: String,
    onNavigateBack: () -> Unit,
    onSymbolSelected: (String) -> Unit,
    onSymbolCreated: (SymbolUiModel) -> Unit = {},
    viewModel: br.com.falacomigo.feature.communication.CommunicationViewModel = hiltViewModel()
) {
    val communicationState by viewModel.state.collectAsState()
    val availableSymbols = communicationState.catalogSymbols.ifEmpty { SeedSymbols.symbols }
    var showSearch by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escolher Símbolo") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ColorTokens.Surface)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showSearch = true },
                containerColor = ColorTokens.Secondary,
                text = { Text("Buscar ARASAAC") },
                icon = { Icon(Icons.Default.Search, null) }
            )
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(availableSymbols) { symbol ->
                Card(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable { 
                            viewModel.onSymbolClick(symbol)
                            onSymbolSelected(symbol.id) 
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ColorTokens.SymbolCardBackground)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
                            val model = symbol.resolveImageModel(LocalContext.current)
                            if (model != null) {
                                AsyncImage(
                                    model = model,
                                    contentDescription = symbol.label,
                                    modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Fit
                                )
                            }
                            Text(
                                symbol.label,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        if (showSearch) {
            PictogramSearchBottomSheet(
                onConfirm = { label, imageUrl ->
                    showSearch = false
                    val newSymbol = SymbolUiModel(
                        id = "custom_${System.currentTimeMillis()}",
                        label = label,
                        spokenText = label,
                        categoryId = "custom",
                        imageUrl = imageUrl,
                        isCustom = true
                    )
                    onSymbolCreated(newSymbol)
                },
                onDismiss = { showSearch = false },
                commViewModel = viewModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PictogramSearchBottomSheet(
    onConfirm: (label: String, imageUrl: String) -> Unit,
    onDismiss: () -> Unit,
    viewModel: ArasaacSearchViewModel = hiltViewModel(),
    commViewModel: br.com.falacomigo.feature.communication.CommunicationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Escolha um pictograma",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = state.query,
                onValueChange = { viewModel.onEvent(ArasaacSearchEvent.QueryChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ex: banana, feliz, escola...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (state.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    else if (state.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onEvent(ArasaacSearchEvent.ClearSearch) }) {
                            Icon(Icons.Default.Clear, null)
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (state.results.isNotEmpty()) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 85.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.results, key = { it.id }) { picto ->
                            PictogramResultCard(
                                pictogram = picto,
                                isSelected = state.selected?.id == picto.id,
                                onClick = { 
                                    commViewModel.onSymbolClick(SymbolUiModel(
                                        id = picto.id.toString(),
                                        label = picto.label,
                                        spokenText = picto.label,
                                        imageUrl = picto.imageUrl
                                    ))
                                    viewModel.onEvent(ArasaacSearchEvent.PictogramSelected(picto)) 
                                }
                            )
                        }
                    }
                } else if (state.query.length >= 2 && !state.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                         Text("Nenhum pictograma encontrado", color = ColorTokens.OnSurfaceVariant)
                    }
                }
            }

            AnimatedVisibility(visible = state.selected != null) {
                state.selected?.let { picto ->
                    Surface(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = ColorTokens.PrimaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = picto.imageUrl,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text(picto.label, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text("Toque em Confirmar", style = MaterialTheme.typography.labelSmall)
                            }
                            Button(
                                onClick = { 
                                    viewModel.savePictogram(picto)
                                    onConfirm(picto.label, picto.imageUrl) 
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Confirmar")
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PictogramResultCard(
    pictogram: ArasaacPictogram,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .border(2.dp, if (isSelected) ColorTokens.Primary else Color.Transparent, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) ColorTokens.PrimaryContainer else ColorTokens.SurfaceVariant
        )
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            AsyncImage(
                model = pictogram.imageUrl,
                contentDescription = pictogram.label,
                modifier = Modifier.fillMaxSize().padding(8.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotEditorScreen(
    boardId: String,
    symbolId: String,
    onNavigateBack: () -> Unit,
    viewModel: BoardEditorViewModel = hiltViewModel(),
    commViewModel: br.com.falacomigo.feature.communication.CommunicationViewModel = hiltViewModel()
) {
    val symbolState by viewModel.currentSymbol.collectAsState()
    var showSearch by remember { mutableStateOf(false) }
    
    var label by remember(symbolState) { mutableStateOf(symbolState?.label ?: "") }
    var spokenText by remember(symbolState) { mutableStateOf(symbolState?.spokenText ?: "") }
    var selectedImageUrl by remember(symbolState) { mutableStateOf<String?>(null) }

    LaunchedEffect(symbolId) {
        viewModel.loadSymbol(symbolId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Símbolo") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            symbolState?.let {
                                val imageChanged = selectedImageUrl != null
                                viewModel.updateSymbol(it.copy(
                                    label = label,
                                    spokenText = spokenText,
                                    imageUrl = selectedImageUrl ?: it.imageUrl,
                                    localImagePath = if (imageChanged) null else it.localImagePath,
                                    thumbnailPath = if (imageChanged) null else it.thumbnailPath,
                                    imageDownloadStatus = if (imageChanged) "PENDING" else it.imageDownloadStatus
                                ))
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Salvar")
                    }
                }
            )
        }
    ) { paddingValues ->
        val symbol = symbolState
        if (symbol == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.size(160.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ColorTokens.SymbolCardBackground),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    val model = selectedImageUrl ?: symbol.resolveImageModel(LocalContext.current)
                    if (model != null) {
                        AsyncImage(
                            model = model,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().padding(16.dp)
                        )
                    } else {
                        Icon(Icons.Default.Image, null, modifier = Modifier.size(64.dp), tint = ColorTokens.OnSurfaceVariant.copy(alpha = 0.3f))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { showSearch = true },
                colors = ButtonDefaults.buttonColors(containerColor = ColorTokens.Secondary),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Search, null)
                Spacer(Modifier.width(8.dp))
                Text("Trocar Imagem (ARASAAC)")
            }

            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Nome que aparece") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = spokenText,
                onValueChange = { spokenText = it },
                label = { Text("O que o app vai falar") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                trailingIcon = {
                    IconButton(onClick = { commViewModel.onSymbolClick(symbol.copy(spokenText = spokenText)) }) {
                        Icon(Icons.Default.PlayArrow, "Ouvir", tint = ColorTokens.Primary)
                    }
                }
            )

            if (showSearch) {
                PictogramSearchBottomSheet(
                    onConfirm = { _, newUrl ->
                        selectedImageUrl = newUrl
                        showSearch = false
                    },
                    onDismiss = { showSearch = false },
                    commViewModel = commViewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoveSymbolScreen(
    boardId: String,
    onNavigateBack: () -> Unit,
    viewModel: BoardEditorViewModel = hiltViewModel()
) {
    val boardState by viewModel.board.collectAsState()
    var items by remember { mutableStateOf<List<SymbolUiModel>>(emptyList()) }

    LaunchedEffect(boardId) {
        viewModel.loadBoard(boardId)
    }

    LaunchedEffect(boardState) {
        boardState?.symbols?.let { items = it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mover Símbolos") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            viewModel.reorderSymbols(boardId, items)
                            onNavigateBack()
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Salvar")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (items.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(items) { index, symbol ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = ColorTokens.SurfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val model = symbol.resolveImageModel(LocalContext.current)
                            AsyncImage(
                                model = model,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Fit
                            )
                            
                            Spacer(Modifier.width(12.dp))
                            
                            Text(
                                text = symbol.label,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(
                                onClick = {
                                    if (index > 0) {
                                        val newList = items.toMutableList()
                                        val item = newList.removeAt(index)
                                        newList.add(index - 1, item)
                                        items = newList
                                    }
                                },
                                enabled = index > 0
                            ) {
                                Icon(Icons.Default.ArrowUpward, "Subir")
                            }

                            IconButton(
                                onClick = {
                                    if (index < items.size - 1) {
                                        val newList = items.toMutableList()
                                        val item = newList.removeAt(index)
                                        newList.add(index + 1, item)
                                        items = newList
                                    }
                                },
                                enabled = index < items.size - 1
                            ) {
                                Icon(Icons.Default.ArrowDownward, "Descer")
                            }
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Lock, null, modifier = Modifier.size(64.dp), tint = ColorTokens.Primary)
            Spacer(Modifier.height(24.dp))
            Text("Acesso Restrito", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Esta área permite editar pranchas e símbolos.",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
                color = ColorTokens.OnSurfaceVariant
            )

            Button(
                modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                onClick = { onEditorVerified("comunicacao") },
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                Text("Entrar no Editor", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
