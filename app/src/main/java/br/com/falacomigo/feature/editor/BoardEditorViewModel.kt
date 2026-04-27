package br.com.falacomigo.feature.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.falacomigo.core.model.BoardUiModel
import br.com.falacomigo.core.model.RoutineUiModel
import br.com.falacomigo.core.model.SymbolUiModel
import br.com.falacomigo.core.seed.SeedSymbols
import br.com.falacomigo.data.repository.BoardRepository
import br.com.falacomigo.data.repository.RoutineRepository
import br.com.falacomigo.data.repository.SymbolRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BoardEditorViewModel @Inject constructor(
    private val boardRepository: BoardRepository,
    private val symbolRepository: SymbolRepository,
    private val routineRepository: RoutineRepository
) : ViewModel() {

    private val _board = MutableStateFlow<BoardUiModel?>(null)
    val board: StateFlow<BoardUiModel?> = _board.asStateFlow()

    fun loadBoard(boardId: String) {
        viewModelScope.launch {
            _board.value = boardRepository.getBoardWithSymbols(boardId)
        }
    }

    private val _currentSymbol = MutableStateFlow<SymbolUiModel?>(null)
    val currentSymbol: StateFlow<SymbolUiModel?> = _currentSymbol.asStateFlow()

    private val _currentRoutine = MutableStateFlow<RoutineUiModel?>(null)
    val currentRoutine: StateFlow<RoutineUiModel?> = _currentRoutine.asStateFlow()

    fun loadRoutine(routineId: String) {
        viewModelScope.launch {
            _currentRoutine.value = routineRepository.getRoutineById(routineId)
        }
    }

    fun updateRoutine(routine: RoutineUiModel) {
        viewModelScope.launch {
            routineRepository.saveRoutine(routine)
            _currentRoutine.value = routine
        }
    }

    fun loadSymbol(symbolId: String) {
        viewModelScope.launch {
            _currentSymbol.value = symbolRepository.getSymbolById(symbolId) ?: SeedSymbols.findById(symbolId)
        }
    }

    fun updateSymbol(symbol: SymbolUiModel) {
        viewModelScope.launch {
            // Salva as alterações (label, spokenText, imageUrl) no banco
            symbolRepository.saveSymbol(symbol)
            _currentSymbol.value = symbol
            
            // Se o símbolo faz parte da prancha atual, recarrega para atualizar o grid
            _board.value?.id?.let { loadBoard(it) }
        }
    }

    fun addSymbol(boardId: String, symbol: SymbolUiModel) {
        viewModelScope.launch {
            // Garante que o símbolo exista globalmente
            symbolRepository.saveSymbol(symbol)
            
            val currentSymbols = _board.value?.symbols ?: emptyList()
            boardRepository.addSymbolToBoard(boardId, symbol.id, currentSymbols.size)
            
            // Recarrega explicitamente
            loadBoard(boardId)
        }
    }

    fun addSymbolById(boardId: String, symbolId: String) {
        viewModelScope.launch {
            val currentSymbols = _board.value?.symbols ?: emptyList()
            boardRepository.addSymbolToBoard(boardId, symbolId, currentSymbols.size)
            loadBoard(boardId)
        }
    }

    fun reorderSymbols(boardId: String, newSymbols: List<SymbolUiModel>) {
        viewModelScope.launch {
            boardRepository.reorderBoardSymbols(boardId, newSymbols)
            // Atualiza o estado local para refletir na UI imediatamente
            _board.value = _board.value?.copy(symbols = newSymbols)
        }
    }
}