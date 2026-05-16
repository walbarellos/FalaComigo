package br.com.falacomigo.feature.communication

import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.VolumeOff
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.falacomigo.R
import br.com.falacomigo.core.designsystem.components.BoardGrid
import br.com.falacomigo.core.designsystem.components.SymbolCard
import br.com.falacomigo.core.designsystem.tokens.ColorTokens
import br.com.falacomigo.core.model.BoardLayoutMode
import br.com.falacomigo.core.model.FavoritePhrase
import br.com.falacomigo.core.model.RoutineUiModel
import br.com.falacomigo.core.model.SymbolCategory
import br.com.falacomigo.core.model.SymbolUiModel
import kotlin.math.roundToInt

private enum class CommunicationTab(val label: String, val icon: ImageVector) {
    INICIO("Início", Icons.Default.GridView),
    ROTINAS("Gestão", Icons.AutoMirrored.Filled.LibraryBooks),
    FAVORITOS("Favoritos", Icons.Default.Favorite)
}

private enum class CaregiverGateAction {
    ORGANIZAR,
    GESTAO
}

private val AppBackground = Color(0xFFF0F4FF)
private val Brand = Color(0xFF6366F1)
private val ActiveBlue = Color(0xFF2563EB)
private val TextPrimary = Color(0xFF0F172A)
private val TextMuted = Color(0xFF64748B)
private val TextSoft = Color(0xFF94A3B8)
private val SoftBorder = Color(0xFFE8EEFF)
private val PillBackground = Color(0xFFF1F5F9)
private val DangerRed = Color(0xFFDC2626)

private data class FilterChipTheme(val accent: Color)

private fun filterTheme(id: String): FilterChipTheme = when (id) {
    "comunicacao" -> FilterChipTheme(Brand)
    "recentes" -> FilterChipTheme(Color(0xFF64748B))
    "necessidades", "alimentacao", "atividades" -> FilterChipTheme(ActiveBlue)
    "social" -> FilterChipTheme(Color(0xFF7C3AED))
    "emocoes" -> FilterChipTheme(Color(0xFFEA580C))
    "numeral" -> FilterChipTheme(Color(0xFF0891B2))
    else -> FilterChipTheme(Brand)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunicationScreen(
    onNavigateToEmergency: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: CommunicationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    val currentBoard = state.currentBoard
    val isRoutineBoard = currentBoard.id.startsWith("routine_")

    if (state.isBootstrappingImages && currentBoard.symbols.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = ColorTokens.Primary)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Preparando símbolos…", fontWeight = FontWeight.Bold)
                if (state.totalCriticalImages > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { state.bootstrapProgress },
                        modifier = Modifier.width(220.dp),
                        color = ColorTokens.Primary,
                        trackColor = ColorTokens.SurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${state.readyImageCount} / ${state.totalCriticalImages}",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
        return
    }

    var selectedTab by remember { mutableStateOf(CommunicationTab.INICIO) }
    var isEditMode by remember { mutableStateOf(false) }
    var phraseSymbols by remember { mutableStateOf<List<SymbolUiModel>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var caregiverUnlocked by remember { mutableStateOf(false) }
    var pendingCaregiverAction by remember { mutableStateOf<CaregiverGateAction?>(null) }

    BackHandler(enabled = isEditMode || currentBoard.id != "comunicacao" || selectedTab != CommunicationTab.INICIO) {
        if (isEditMode) isEditMode = false
        else if (selectedTab != CommunicationTab.INICIO) selectedTab = CommunicationTab.INICIO
        else viewModel.selectBoard("comunicacao")
    }

    fun runCaregiverAction(action: CaregiverGateAction) {
        when (action) {
            CaregiverGateAction.ORGANIZAR -> {
                selectedTab = CommunicationTab.INICIO
                isEditMode = true
            }
            CaregiverGateAction.GESTAO -> {
                selectedTab = CommunicationTab.ROTINAS
                isEditMode = false
            }
        }
    }

    fun requestCaregiverAccess(action: CaregiverGateAction) {
        if (caregiverUnlocked) {
            runCaregiverAction(action)
        } else {
            pendingCaregiverAction = action
        }
    }

    val visibleSymbols = remember(currentBoard.symbols, searchQuery) {
        val query = searchQuery.trim()
        if (query.isBlank()) {
            currentBoard.symbols
        } else {
            currentBoard.symbols.filter { symbol ->
                symbol.label.contains(query, ignoreCase = true) ||
                    symbol.spokenText.contains(query, ignoreCase = true) ||
                    symbol.category.title.contains(query, ignoreCase = true)
            }
        }
    }

    Scaffold(
        containerColor = ColorTokens.Background,
        topBar = {
            FalaComigoTopBar(
                selectedTab = selectedTab,
                boardTitle = currentBoard.title,
                isEditMode = isEditMode,
                isRootBoard = currentBoard.id == "comunicacao",
                onBack = { viewModel.selectBoard("comunicacao") },
                onOrganize = { requestCaregiverAccess(CaregiverGateAction.ORGANIZAR) },
                onCloseEdit = { isEditMode = false },
                onSettings = onNavigateToSettings,
                onEmergency = onNavigateToEmergency
            )
        },
        bottomBar = {
            FalaComigoBottomBar(
                selectedTab = selectedTab,
                onSelect = {
                    if (it == CommunicationTab.ROTINAS) {
                        requestCaregiverAccess(CaregiverGateAction.GESTAO)
                    } else {
                        selectedTab = it
                        isEditMode = false
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ColorTokens.Background)
        ) {
            if (selectedTab == CommunicationTab.INICIO && !isEditMode) {
                PhraseComposerBar(
                    phraseSymbols = phraseSymbols,
                    isSpeaking = state.isSpeaking,
                    onRemoveAt = { index -> phraseSymbols = phraseSymbols.filterIndexed { i, _ -> i != index } },
                    onBackspace = { if (phraseSymbols.isNotEmpty()) phraseSymbols = phraseSymbols.dropLast(1) },
                    onClear = { phraseSymbols = emptyList() },
                    onSpeak = {
                        val text = phraseSymbols.joinToString(" ") { symbol ->
                            symbol.spokenText.ifBlank { symbol.label }
                        }.trim()
                        if (text.isNotBlank()) {
                            viewModel.onSymbolClick(SymbolUiModel(id = "phrase_${System.currentTimeMillis()}", label = text, spokenText = text))
                        }
                    },
                    onFavorite = {
                        val text = phraseSymbols.joinToString(" ") { symbol ->
                            symbol.spokenText.ifBlank { symbol.label }
                        }.trim()
                        viewModel.saveFavoritePhrase(text)
                    }
                )
                SpeechErrorBanner(
                    message = state.speechErrorMessage,
                    onDismiss = viewModel::clearSpeechError
                )
            }

            if (selectedTab == CommunicationTab.INICIO && !isRoutineBoard) {
                BoardSelectorRow(currentBoardId = currentBoard.id, onBoardSelect = viewModel::selectBoard)
            }

            if (selectedTab == CommunicationTab.INICIO && !isEditMode) {
                BoardSearchBar(query = searchQuery, onQueryChange = { searchQuery = it })
            }

            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    CommunicationTab.INICIO -> {
                        if (visibleSymbols.isEmpty() && searchQuery.isNotBlank()) {
                            SearchEmptyState(query = searchQuery, onClear = { searchQuery = "" })
                        } else {
                            InicioTab(
                                symbols = visibleSymbols,
                                boardId = currentBoard.id,
                                groupedSymbols = if (searchQuery.isBlank()) state.groupedSymbols else visibleSymbols.groupBy { it.category },
                                speakingId = state.speakingSymbolId,
                                layoutMode = state.layoutMode,
                                vibration = state.vibrationEnabled,
                                cardSizeScale = state.cardSizeScale,
                                highContrast = state.highContrastEnabled,
                                isEditMode = isEditMode,
                                onClick = { symbol ->
                                    if (symbol.id.startsWith("routine_")) {
                                        viewModel.onSymbolClick(symbol)
                                    } else {
                                        phraseSymbols = (phraseSymbols + symbol).takeLast(24)
                                        if (state.speakOnTapEnabled) {
                                            viewModel.onSymbolClick(symbol)
                                        } else {
                                            viewModel.recordSymbolUse(symbol)
                                        }
                                    }
                                },
                                onWarmUp = viewModel::warmUpTts,
                                onMove = viewModel::moveSymbol
                            )
                        }
                    }
                    CommunicationTab.ROTINAS -> RotinasTab(state, viewModel)
                    CommunicationTab.FAVORITOS -> FavoritosTab(
                        favorites = state.favorites,
                        onFavoriteClick = viewModel::onFavoritePhraseClick,
                        onDeleteFavorite = viewModel::deleteFavoritePhrase
                    )
                }
            }
        }
    }

    pendingCaregiverAction?.let { action ->
        CaregiverGateDialog(
            pin = state.editorPin,
            onDismiss = { pendingCaregiverAction = null },
            onVerified = {
                caregiverUnlocked = true
                pendingCaregiverAction = null
                runCaregiverAction(action)
            }
        )
    }
}

@Composable
private fun FalaComigoTopBar(
    selectedTab: CommunicationTab,
    boardTitle: String,
    isEditMode: Boolean,
    isRootBoard: Boolean,
    onBack: () -> Unit,
    onOrganize: () -> Unit,
    onCloseEdit: () -> Unit,
    onSettings: () -> Unit,
    onEmergency: () -> Unit
) {
    val title = when {
        isEditMode -> "Organizar"
        selectedTab == CommunicationTab.ROTINAS -> "Gestão"
        selectedTab == CommunicationTab.FAVORITOS -> "Favoritos"
        !isRootBoard -> boardTitle
        else -> "FalaComigo"
    }

    Surface(color = Color.White) {
        Column {
            BoxWithConstraints {
                val compact = maxWidth < 390.dp
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isEditMode) {
                        IconButton(onClick = onCloseEdit, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar", tint = TextPrimary)
                        }
                    } else if (!isRootBoard && selectedTab == CommunicationTab.INICIO) {
                        IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = TextPrimary)
                        }
                    } else {
                        Image(
                            painter = painterResource(R.drawable.topbar_app_icon),
                            contentDescription = "FalaComigo",
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp, end = 8.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = title,
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!compact && !isEditMode && isRootBoard && selectedTab == CommunicationTab.INICIO) {
                            Text(
                                text = "Comunicação Alternativa",
                                color = TextSoft,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (isEditMode) {
                        HeaderActionButton(label = "Concluir", onClick = onCloseEdit)
                    } else {
                        HeaderActionButton(label = if (compact) "Org" else "Organizar", onClick = onOrganize)
                        Spacer(Modifier.width(8.dp))
                        HeaderActionButton(label = "Config", onClick = onSettings)
                        Spacer(Modifier.width(8.dp))
                        HeaderActionButton(label = "SOS", onClick = onEmergency, danger = true)
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(SoftBorder)
            )
        }
    }
}

@Composable
private fun CaregiverGateDialog(
    pin: String,
    onDismiss: () -> Unit,
    onVerified: () -> Unit
) {
    var typedPin by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ColorTokens.Surface,
        title = {
            Text(
                text = "Área do cuidador",
                color = ColorTokens.OnSurface,
                fontWeight = FontWeight.ExtraBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Digite o PIN para alterar a prancha.",
                    color = ColorTokens.OnSurfaceVariant,
                    fontSize = 13.sp
                )
                OutlinedTextField(
                    value = typedPin,
                    onValueChange = {
                        typedPin = it.filter { char -> char.isDigit() }.take(8)
                        hasError = false
                    },
                    singleLine = true,
                    isError = hasError,
                    label = { Text("PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ColorTokens.Primary,
                        focusedLabelColor = ColorTokens.Primary,
                        cursorColor = ColorTokens.Primary,
                        focusedTextColor = ColorTokens.OnSurface,
                        unfocusedTextColor = ColorTokens.OnSurface
                    )
                )
                if (hasError) {
                    Text("PIN incorreto.", color = ColorTokens.Error, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (typedPin == pin) {
                        onVerified()
                    } else {
                        hasError = true
                    }
                },
                enabled = typedPin.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ColorTokens.Primary)
            ) {
                Text("Entrar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = ColorTokens.OnSurfaceVariant)
            }
        }
    )
}

@Composable
private fun HeaderActionButton(
    label: String,
    onClick: () -> Unit,
    danger: Boolean = false
) {
    val shape = RoundedCornerShape(9.dp)
    Box(
        modifier = Modifier
            .height(34.dp)
            .clip(shape)
            .background(if (danger) DangerRed else Color.White)
            .border(1.dp, if (danger) DangerRed else Color(0xFFD9E2F5), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = if (danger) 16.dp else 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (danger) Color.White else Color(0xFF475569),
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1
        )
    }
}

@Composable
private fun SpeechErrorBanner(
    message: String?,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(visible = message != null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFFF1F2))
                .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeOff,
                contentDescription = null,
                tint = DangerRed,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = message.orEmpty(),
                color = Color(0xFF7F1D1D),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Fechar aviso",
                    tint = Color(0xFF7F1D1D),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun PhraseComposerBar(
    phraseSymbols: List<SymbolUiModel>,
    isSpeaking: Boolean,
    onRemoveAt: (Int) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onSpeak: () -> Unit,
    onFavorite: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "FRASE",
                color = TextSoft,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f)
            )
            PhraseIconActionButton(
                icon = Icons.Default.StarBorder,
                enabled = phraseSymbols.isNotEmpty(),
                onClick = onFavorite
            )
            Spacer(Modifier.width(6.dp))
            PhraseActionButton(label = "Apagar", enabled = phraseSymbols.isNotEmpty(), onClick = onBackspace)
            Spacer(Modifier.width(6.dp))
            PhraseActionButton(label = "Falar", enabled = phraseSymbols.isNotEmpty() && !isSpeaking, filled = true, onClick = onSpeak)
            Spacer(Modifier.width(6.dp))
            PhraseActionButton(label = "Limpar", enabled = phraseSymbols.isNotEmpty(), onClick = onClear)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .height(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF8FAFF))
                .border(1.dp, Color(0xFFDDE7FA), RoundedCornerShape(12.dp))
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (phraseSymbols.isEmpty()) {
                Text(
                    text = "Toque nos símbolos para montar uma frase...",
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            } else {
                phraseSymbols.forEachIndexed { index, symbol ->
                    PhraseWordChip(text = symbol.label, onClick = { onRemoveAt(index) })
                }
            }
        }
    }
}

@Composable
private fun PhraseIconActionButton(
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(9.dp)
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(shape)
            .background(if (enabled) Color.White else Color(0xFFF1F5F9))
            .border(1.dp, if (enabled) Color(0xFFDDE7FA) else Color(0xFFF1F5F9), shape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Salvar favorito",
            tint = if (enabled) Brand else Color(0xFFCBD5E1),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun PhraseActionButton(
    label: String,
    enabled: Boolean,
    filled: Boolean = false,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(9.dp)
    val background = when {
        !enabled -> Color(0xFFF1F5F9)
        filled -> Brand
        else -> Color.White
    }
    val foreground = when {
        !enabled -> Color(0xFFCBD5E1)
        filled -> Color.White
        else -> Color(0xFF94A3B8)
    }
    Box(
        modifier = Modifier
            .height(28.dp)
            .clip(shape)
            .background(background)
            .border(1.dp, if (enabled && !filled) Color(0xFFDDE7FA) else background, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = foreground, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PhraseWordChip(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(Color(0xFFEEF2FF))
            .border(1.dp, Color(0xFFD9E2FF), RoundedCornerShape(99.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = Color(0xFF4338CA),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
private fun BoardSearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(54.dp),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSoft) },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Limpar busca", tint = TextSoft)
                }
            }
        },
        placeholder = {
            Text(
                text = "Buscar símbolo...",
                color = TextSoft,
                fontSize = 13.sp
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            focusedBorderColor = SoftBorder,
            unfocusedBorderColor = SoftBorder,
            cursorColor = Brand,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        )
    )
}

@Composable
private fun SearchEmptyState(query: String, onClear: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, SoftBorder, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.SearchOff, contentDescription = null, tint = TextSoft)
            }
            Text(
                text = "Não encontramos símbolos para \"$query\".",
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp
            )
            HeaderActionButton(label = "Limpar busca", onClick = onClear)
        }
    }
}

@Composable
private fun FalaComigoBottomBar(
    selectedTab: CommunicationTab,
    onSelect: (CommunicationTab) -> Unit
) {
    Surface(color = Color.White) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(SoftBorder)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                CommunicationTab.entries.forEach { tab ->
                    val active = selectedTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onSelect(tab) },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .width(44.dp)
                                .height(3.dp)
                                .clip(RoundedCornerShape(bottomStart = 99.dp, bottomEnd = 99.dp))
                                .background(if (active) Brand else Color.Transparent)
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = null,
                                tint = if (active) Brand else TextSoft,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = tab.label,
                                color = if (active) Brand else TextMuted,
                                fontSize = 10.sp,
                                fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BoardSelectorRow(currentBoardId: String, onBoardSelect: (String) -> Unit) {
    val chips = remember {
        listOf(
            "comunicacao" to "Prancha",
            "recentes" to "Recentes",
            "necessidades" to "Necessidades",
            "social" to "Social",
            "emocoes" to "Emoções",
            "numeral" to "Números",
            "alimentacao" to "Comer",
            "atividades" to "Lazer"
        )
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        chips.forEach { (id, label) ->
            val onSelect = remember(id) { { onBoardSelect(id) } }
            FilterPill(
                label = label,
                selected = currentBoardId == id,
                theme = filterTheme(id),
                onClick = onSelect
            )
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    selected: Boolean,
    theme: FilterChipTheme,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(99.dp)
    Row(
        modifier = Modifier
            .height(30.dp)
            .clip(shape)
            .background(if (selected) theme.accent else PillBackground)
            .border(1.dp, if (selected) theme.accent else Color.Transparent, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (!selected) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(theme.accent)
            )
        }
        Text(
            text = label,
            color = if (selected) Color.White else Color(0xFF5B6B84),
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1
        )
    }
}

@Composable
private fun InicioTab(
    symbols: List<SymbolUiModel>, 
    boardId: String, 
    groupedSymbols: Map<SymbolCategory, List<SymbolUiModel>>,
    speakingId: String?, 
    layoutMode: BoardLayoutMode, 
    vibration: Boolean, 
    cardSizeScale: Float,
    highContrast: Boolean,
    isEditMode: Boolean,
    onClick: (SymbolUiModel) -> Unit,
    onWarmUp: () -> Unit,
    onMove: (Int, Int) -> Unit
) {
    BoardGrid(
        symbols = symbols,
        boardId = boardId,
        groupedSymbols = groupedSymbols,
        isEditMode = isEditMode,
        layoutMode = layoutMode, 
        speakingSymbolId = speakingId,
        vibrationEnabled = vibration,
        cardSizeScale = cardSizeScale,
        highContrast = highContrast,
        onSymbolClick = onClick,
        onWarmUp = onWarmUp,
        onMove = onMove
    )
}

@Composable
private fun RotinasTab(state: CommunicationState, viewModel: CommunicationViewModel) {
    var showCreateRoutine by remember { mutableStateOf(false) }
    var showWordCreator by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorTokens.Background),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Grupos de Acesso Rápido",
                    style = MaterialTheme.typography.titleSmall,
                    color = ColorTokens.Primary,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            if (state.routines.isEmpty()) {
                item {
                    EmptyContentPanel(
                        icon = Icons.AutoMirrored.Filled.LibraryBooks,
                        title = "Nenhuma rotina configurada.",
                        body = "Crie grupos para reunir símbolos usados juntos."
                    )
                }
            }
            items(state.routines, key = { it.id }) { routine ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ColorTokens.OutlineVariant, RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    color = ColorTokens.Surface,
                    tonalElevation = 0.dp,
                    shadowElevation = 1.dp,
                    onClick = { viewModel.openRoutineAsBoard(routine) }
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(routine.title, color = ColorTokens.OnSurface, fontWeight = FontWeight.ExtraBold)
                            Text("${routine.symbols.size} itens", color = ColorTokens.OnSurfaceVariant, fontSize = 12.sp)
                        }
                        IconButton(onClick = { viewModel.startEditRoutine(routine) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = ColorTokens.Primary)
                        }
                        IconButton(onClick = { viewModel.deleteRoutine(routine.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = ColorTokens.Error)
                        }
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
        RoutineManagerDialog(title = if (state.editingRoutine == null) "Nova Rotina" else "Editar Rotina", routine = state.editingRoutine, initialSymbols = state.editingRoutineSymbols, searchResults = state.searchResults, isSearching = state.isSearching, onDismiss = { if (state.editingRoutine != null) viewModel.clearEditRoutine() else showCreateRoutine = false }, onSave = { n, s -> viewModel.saveRoutine(n, s); showCreateRoutine = false }, onSearch = viewModel::onSearchQueryChanged)
    }
    if (showWordCreator) {
        WordCreatorDialog(onDismiss = { showWordCreator = false }, onWordCreated = { viewModel.saveSymbol(it); showWordCreator = false }, onSearch = viewModel::onSearchQueryChanged, searchResults = state.searchResults, isSearching = state.isSearching)
    }
}

@Composable
private fun RoutineManagerDialog(title: String, routine: RoutineUiModel?, initialSymbols: List<SymbolUiModel>, searchResults: List<SymbolUiModel>, isSearching: Boolean, onDismiss: () -> Unit, onSave: (String, List<SymbolUiModel>) -> Unit, onSearch: (String) -> Unit) {
    var name by remember(routine) { mutableStateOf(routine?.title ?: "") }
    var selectedSymbols by remember(initialSymbols) { mutableStateOf(initialSymbols) }
    var query by remember { mutableStateOf("") }
    var showWordCreatorInRoutine by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss, 
        containerColor = ColorTokens.Surface,
        title = { Text(title, color = ColorTokens.OnSurface, fontWeight = FontWeight.ExtraBold) }, 
        text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = name, 
                onValueChange = { name = it }, 
                label = { Text("Nome") }, 
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = ColorTokens.OnSurface,
                    unfocusedTextColor = ColorTokens.OnSurface,
                    focusedBorderColor = ColorTokens.Primary,
                    focusedLabelColor = ColorTokens.Primary
                )
            )
            HorizontalDivider(color = ColorTokens.OutlineVariant)
            Text("Itens Selecionados:", style = MaterialTheme.typography.labelLarge, color = ColorTokens.Primary)
            if (selectedSymbols.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.height(70.dp)) {
                    items(selectedSymbols, key = { it.id }) { s ->
                        Box(modifier = Modifier.size(60.dp)) { SymbolCard(symbol = s, isSmall = true, onClick = { selectedSymbols = selectedSymbols - s }) }
                    }
                }
            } else {
                Text("Adicione palavras abaixo.", fontSize = 11.sp, color = ColorTokens.OnSurfaceVariant)
            }
            OutlinedTextField(
                value = query, 
                onValueChange = { query = it; onSearch(it) }, 
                placeholder = { Text("Buscar no catálogo...") }, 
                modifier = Modifier.fillMaxWidth(), 
                trailingIcon = { if (isSearching) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else Icon(Icons.Default.Search, null) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = ColorTokens.OnSurface,
                    unfocusedTextColor = ColorTokens.OnSurface,
                    focusedBorderColor = ColorTokens.Primary
                )
            )
            if (searchResults.isNotEmpty()) {
                Text("Resultados:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColorTokens.OnSurface)
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
    AlertDialog(
        onDismissRequest = onDismiss, 
        containerColor = ColorTokens.Surface,
        title = { Text("Nova Palavra", color = ColorTokens.OnSurface, fontWeight = FontWeight.Bold) }, 
        text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = label, 
                onValueChange = { label = it; if(talk.isEmpty()) talk = it }, 
                label = { Text("Nome") }, 
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = ColorTokens.OnSurface,
                    unfocusedTextColor = ColorTokens.OnSurface,
                    focusedBorderColor = ColorTokens.Primary,
                    focusedLabelColor = ColorTokens.Primary
                )
            )
            OutlinedTextField(
                value = talk, 
                onValueChange = { talk = it }, 
                label = { Text("Voz") }, 
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = ColorTokens.OnSurface,
                    unfocusedTextColor = ColorTokens.OnSurface,
                    focusedBorderColor = ColorTokens.Primary,
                    focusedLabelColor = ColorTokens.Primary
                )
            )
            Text("Imagem:", style = MaterialTheme.typography.labelMedium, color = ColorTokens.OnSurface)
            OutlinedTextField(
                value = query, 
                onValueChange = { query = it; onSearch(it) }, 
                placeholder = { Text("Pesquisar...") }, 
                modifier = Modifier.fillMaxWidth(), 
                trailingIcon = { if (isSearching) CircularProgressIndicator(modifier = Modifier.size(20.dp)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = ColorTokens.OnSurface,
                    unfocusedTextColor = ColorTokens.OnSurface,
                    focusedBorderColor = ColorTokens.Primary
                )
            )
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
private fun FavoritosTab(
    favorites: List<FavoritePhrase>,
    onFavoriteClick: (FavoritePhrase) -> Unit,
    onDeleteFavorite: (String) -> Unit
) {
    if (favorites.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBackground)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            EmptyContentPanel(
                icon = Icons.Default.FavoriteBorder,
                title = "Os favoritos aparecerão aqui quando forem configurados.",
                body = "Use esta área para frases e respostas recorrentes."
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(favorites, key = { it.id }) { fav ->
            Surface(
                onClick = { onFavoriteClick(fav) },
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFD9E2FF), RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFEEF2FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Brand, modifier = Modifier.size(20.dp))
                    }
                    Text(
                        fav.text,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 12.dp),
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    IconButton(onClick = { onDeleteFavorite(fav.id) }) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Remover favorito",
                            tint = TextSoft
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyContentPanel(
    icon: ImageVector,
    title: String,
    body: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFEEF2FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Brand)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                Text(body, color = TextMuted, fontSize = 12.sp)
            }
        }
    }
}
