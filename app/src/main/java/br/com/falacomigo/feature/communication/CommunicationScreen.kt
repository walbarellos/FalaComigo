package br.com.falacomigo.feature.communication

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.falacomigo.core.designsystem.tokens.ColorTokens
import br.com.falacomigo.core.model.SymbolUiModel
import br.com.falacomigo.core.model.Routine
import br.com.falacomigo.core.model.FavoritePhrase

private val SYMBOL_IMAGES = mapOf(
    "eu" to "sym_eu", "feliz" to "sym_feliz", "com_medo" to "sym_com_medo",
    "cansado" to "sym_cansado", "voce" to "sym_voce", "triste" to "sym_triste",
    "frustrado" to "sym_frustrado", "com_fome" to "sym_com_fome", "dor" to "sym_dor",
    "bravo" to "sym_bravo", "machucado" to "sym_machucado", "com_sede" to "sym_com_sede",
    "banheiro" to "sym_banheiro", "agua" to "sym_agua", "ajuda" to "sym_ajuda",
    "quero_parar" to "sym_quero_parar"
)

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
    
    val tabs = listOf("Inicio", "Rotinas", "Favoritos")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = board.title,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = ColorTokens.OnSurface
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Configurações",
                            tint = ColorTokens.OnSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ColorTokens.Surface
                )
            )
        },
        containerColor = ColorTokens.Background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                        text = {
                            Text(
                                title,
                                fontSize = 14.sp,
                                fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        icon = {
                            when (index) {
                                0 -> Icon(Icons.Outlined.GridView, contentDescription = null, modifier = Modifier.size(20.dp))
                                1 -> Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(20.dp))
                                2 -> Icon(Icons.Outlined.Favorite, contentDescription = null, modifier = Modifier.size(20.dp))
                            }
                        },
                        selectedContentColor = ColorTokens.Primary,
                        unselectedContentColor = ColorTokens.OnSurfaceVariant
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(Pair(1, "Lento"), Pair(2, "Normal"), Pair(3, "Rapido")).forEach { (speed, label) ->
                        Surface(
                            onClick = { viewModel.setVoiceSpeed(speed) },
                            shape = RoundedCornerShape(20.dp),
                            color = if (state.voiceSpeed == speed) ColorTokens.Primary else ColorTokens.SurfaceVariant,
                            modifier = Modifier.width(64.dp).heightIn(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Text(
                                    label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (state.voiceSpeed == speed) ColorTokens.OnPrimary else ColorTokens.OnSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            when (selectedTab) {
                0 -> inicioTab(board, viewModel)
                1 -> rotinasTab(viewModel)
                2 -> favoritosTab(viewModel)
            }
        }
    }
}

@Composable
private fun inicioTab(board: br.com.falacomigo.core.model.BoardUiModel, viewModel: CommunicationViewModel) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        items(board.symbols, key = { it.id }) { symbol ->
            CommunicationCard(
                symbol = symbol,
                onClick = { viewModel.onSymbolClick(symbol) }
            )
        }
    }
}

@Composable
private fun rotinasTab(viewModel: CommunicationViewModel) {
    val state by viewModel.state.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            if (state.routines.isEmpty()) {
                Spacer(modifier = Modifier.height(80.dp))
                Text(
                    "Nenhuma rotina criada",
                    style = MaterialTheme.typography.titleMedium,
                    color = ColorTokens.OnSurfaceVariant
                )
                Text(
                    "Toque + para criar sua primeira rotina",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ColorTokens.OnSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                state.routines.forEach { routine ->
                    RoutineCard(
                        routine = routine,
                        onClick = { viewModel.playRoutine(routine) },
                        onDelete = { viewModel.deleteRoutine(routine.id) },
                        onEdit = { viewModel.startEditRoutine(routine) }
                    )
                }
            }
        }
        
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = ColorTokens.Primary
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Criar rotina",
                tint = ColorTokens.OnPrimary
            )
        }
    }
    
    if (showCreateDialog) {
        CreateRoutineDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, phrase ->
                viewModel.createRoutine(name, listOf(phrase))
                showCreateDialog = false
            }
        )
    }

    state.editingRoutine?.let { routine ->
        EditRoutineDialog(
            routine = routine,
            onDismiss = { viewModel.clearEditRoutine() },
            onSave = { name, phrase ->
                viewModel.updateRoutine(routine.id, name, listOf(phrase))
                viewModel.clearEditRoutine()
            }
        )
    }
}

@Composable
private fun CreateRoutineDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit
) {
    var nameField by remember { mutableStateOf("") }
    var phraseField by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Criar Rotina") },
        text = {
            Column {
                OutlinedTextField(
                    value = nameField,
                    onValueChange = { nameField = it },
                    label = { Text("Nome") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phraseField,
                    onValueChange = { phraseField = it },
                    label = { Text("Frase") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(nameField, phraseField) },
                enabled = nameField.isNotBlank() && phraseField.isNotBlank()
            ) {
                Text("Criar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun EditRoutineDialog(
    routine: br.com.falacomigo.core.model.Routine,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var nameField by remember { mutableStateOf(routine.name) }
    var phraseField by remember { mutableStateOf(routine.symbols.joinToString(" ")) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Rotina") },
        text = {
            Column {
                OutlinedTextField(
                    value = nameField,
                    onValueChange = { nameField = it },
                    label = { Text("Nome") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phraseField,
                    onValueChange = { phraseField = it },
                    label = { Text("Frase") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(nameField, phraseField) },
                enabled = nameField.isNotBlank() && phraseField.isNotBlank()
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun favoritosTab(viewModel: CommunicationViewModel) {
    val state by viewModel.state.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (state.favorites.isEmpty()) {
            Text(
                "Nenhum favorito ainda",
                style = MaterialTheme.typography.titleMedium,
                color = ColorTokens.OnSurfaceVariant
            )
            Text(
                "Toque nos símbolos para adicionar aos favoritos",
                style = MaterialTheme.typography.bodyMedium,
                color = ColorTokens.OnSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        } else {
            state.favorites.forEach { fav ->
                FavoriteCard(
                    favorite = fav,
                    onClick = { viewModel.onSymbolClick(br.com.falacomigo.core.model.SymbolUiModel(id = fav.id, label = fav.text, spokenText = fav.text)) }
                )
            }
        }
    }
}

@Composable
private fun RoutineCard(
    routine: br.com.falacomigo.core.model.Routine,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = ColorTokens.SecondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Home,
                contentDescription = null,
                tint = ColorTokens.OnSecondaryContainer,
                modifier = Modifier.size(24.dp)
            )
            Text(
                routine.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f).padding(start = 12.dp)
            )
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = ColorTokens.OnSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Excluir",
                    tint = ColorTokens.OnSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun FavoriteCard(favorite: br.com.falacomigo.core.model.FavoritePhrase, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = ColorTokens.PrimaryContainer
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.Favorite,
                contentDescription = null,
                tint = ColorTokens.OnPrimaryContainer
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(favorite.text, style = MaterialTheme.typography.titleMedium)
                Text("${favorite.clickCount}x usado", style = MaterialTheme.typography.bodySmall, color = ColorTokens.OnSurfaceVariant)
            }
        }
    }
}

@Composable
fun CommunicationCard(
    symbol: SymbolUiModel,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    val cardColor = ColorTokens.getCardColor(symbol.category)
    val imageResId = context.resources.getIdentifier(
        SYMBOL_IMAGES[symbol.id] ?: "",
        "drawable",
        context.packageName
    )

    Surface(
        modifier = Modifier
            .heightIn(min = 110.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (isPressed) 1.dp else 3.dp,
                shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(20.dp),
        color = cardColor,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (imageResId != 0) {
                    Image(
                        painter = painterResource(imageResId),
                        contentDescription = symbol.label,
                        modifier = Modifier.size(68.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(symbol.label.take(2), fontSize = 28.sp, textAlign = TextAlign.Center)
                }
            }
            Text(
                symbol.label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = ColorTokens.OnSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}