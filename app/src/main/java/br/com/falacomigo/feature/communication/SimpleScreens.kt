package br.com.falacomigo.feature.communication

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.falacomigo.core.designsystem.tokens.ColorTokens
import br.com.falacomigo.core.designsystem.tokens.SpacingTokens
import br.com.falacomigo.core.seed.SeedBoards

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyBoardScreen(
    onNavigateBack: () -> Unit,
    viewModel: CommunicationViewModel = hiltViewModel()
) {
    val urgentBoard = SeedBoards.boards.firstOrNull { it.isEmergency } ?: SeedBoards.findById("urgente")!!

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Urgente") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ColorTokens.SecondaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(urgentBoard.columns),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(SpacingTokens.ScreenPadding),
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.GridGap),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.GridGap)
        ) {
            items(urgentBoard.symbols) { symbol ->
                Card(
                    modifier = Modifier
                        .size(100.dp)
                        .clickable { viewModel.onSymbolClick(symbol) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ColorTokens.SecondaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ColorTokens.Secondary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    symbol.label.take(1),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = ColorTokens.OnSecondary
                                )
                            }
                            Text(
                                symbol.label,
                                style = MaterialTheme.typography.bodyMedium,
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
fun BoardSelectorScreen(
    onNavigateBack: () -> Unit,
    onBoardSelected: (String) -> Unit,
    viewModel: CommunicationViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escolher Prancha") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                            contentDescription = "Voltar"
                        )
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
            SeedBoards.boards.forEach { board ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = SpacingTokens.Sm)
                        .clickable { onBoardSelected(board.id) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.padding(SpacingTokens.Md)) {
                        Text(board.title, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}