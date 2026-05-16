package br.com.falacomigo.feature.communication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.falacomigo.core.designsystem.components.SymbolCard
import br.com.falacomigo.core.designsystem.tokens.ColorTokens
import br.com.falacomigo.core.seed.SeedBoards

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyBoardScreen(
    onNavigateBack: () -> Unit,
    viewModel: CommunicationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val previousBoardId = remember { state.currentBoard.id.takeIf { it.isNotBlank() } ?: "comunicacao" }
    LaunchedEffect(Unit) {
        viewModel.selectBoard("urgente")
    }
    DisposableEffect(Unit) {
        onDispose {
            viewModel.selectBoard(previousBoardId)
        }
    }
    val urgentBoard = state.currentBoard.takeIf { it.id == "urgente" && it.symbols.isNotEmpty() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626))
                        Spacer(Modifier.width(8.dp))
                        Text("SOS", color = Color(0xFFDC2626), fontWeight = FontWeight.ExtraBold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    navigationIconContentColor = Color(0xFF0F172A)
                )
            )
        },
        containerColor = ColorTokens.Background
    ) { paddingValues ->
        if (urgentBoard == null || state.isBootstrappingImages) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = ColorTokens.Primary)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(if (state.cardSizeScale > 1.05f) 3 else 4),
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(urgentBoard.symbols, key = { it.id }) { symbol ->
                    SymbolCard(
                        symbol = symbol,
                        vibrationEnabled = state.vibrationEnabled,
                        isSpeaking = state.speakingSymbolId == symbol.id,
                        textScale = state.cardSizeScale,
                        highContrast = state.highContrastEnabled,
                        onClick = { viewModel.onSymbolClick(symbol) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardSelectorScreen(
    onNavigateBack: () -> Unit,
    onBoardSelected: (String) -> Unit,
    viewModel: CommunicationViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escolher Prancha", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = ColorTokens.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SeedBoards.boards.forEach { board ->
                Surface(
                    onClick = { 
                        viewModel.selectBoard(board.id)
                        onBoardSelected(board.id) 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Box(modifier = Modifier.padding(20.dp)) {
                        Text(board.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
