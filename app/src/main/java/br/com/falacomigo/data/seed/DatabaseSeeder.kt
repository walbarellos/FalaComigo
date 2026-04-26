package br.com.falacomigo.data.seed

import br.com.falacomigo.data.repository.BoardRepository
import br.com.falacomigo.data.repository.RoutineRepository
import br.com.falacomigo.data.repository.SymbolRepository
import br.com.falacomigo.core.seed.SeedBoards as SeedBoardsData
import br.com.falacomigo.core.seed.SeedRoutines as SeedRoutinesData
import br.com.falacomigo.core.seed.SeedSymbols as SeedSymbolsData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSeeder @Inject constructor(
    private val symbolRepository: SymbolRepository,
    private val boardRepository: BoardRepository,
    private val routineRepository: RoutineRepository
) {
    suspend fun seedIfEmpty() {
        val symbolCount = symbolRepository.getSymbolCount()
        
        if (symbolCount == 0) {
            seedSymbols()
            seedBoards()
            seedRoutines()
        }
    }

    private suspend fun seedSymbols() {
        val symbols = SeedSymbolsData.symbols.map { seed ->
            br.com.falacomigo.core.model.SymbolUiModel(
                id = seed.id,
                label = seed.label,
                spokenText = seed.spokenText,
                imagePath = seed.imagePath,
                category = seed.category,
                isEmergency = seed.isEmergency,
                isCustom = false,
                accessibilityLabel = seed.accessibilityLabel
            )
        }
        symbolRepository.saveSymbols(symbols)
    }

    private suspend fun seedBoards() {
        val boards = SeedBoardsData.boards.map { seed ->
            br.com.falacomigo.core.model.BoardUiModel(
                id = seed.id,
                title = seed.title,
                description = seed.description,
                symbols = seed.symbols,
                columns = seed.columns,
                isEmergency = seed.isEmergency,
                isDefault = seed.isDefault,
                order = seed.order
            )
        }
        
        boards.forEach { board ->
            boardRepository.saveBoard(board)
            
            board.symbols.forEachIndexed { index, symbol ->
                boardRepository.addSymbolToBoard(board.id, symbol.id, index)
            }
        }
    }

    private suspend fun seedRoutines() {
        val routines = SeedRoutinesData.routines.map { seed ->
            br.com.falacomigo.core.model.RoutineUiModel(
                id = seed.id,
                title = seed.title,
                subtitle = seed.subtitle,
                iconName = seed.iconName,
                boardId = seed.boardId,
                isEmergency = seed.isEmergency,
                order = seed.order
            )
        }
        routineRepository.saveRoutines(routines)
    }
}