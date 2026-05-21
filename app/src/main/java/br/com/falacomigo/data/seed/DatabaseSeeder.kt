package br.com.falacomigo.data.seed

import br.com.falacomigo.data.repository.ArasaacRepository
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
    private val routineRepository: RoutineRepository,
    private val arasaacRepository: ArasaacRepository
) {
    suspend fun seedIfEmpty() {
        val symbolCount = symbolRepository.getSymbolCount()
        
        if (symbolCount == 0) {
            seedSymbols()
            seedBoards()
            seedRoutines()
            return
        }
        
        syncVerifiedSymbolsFromSeed()
        syncSeedBoardsSymbols()
    }

    private suspend fun syncSeedBoardsSymbols() {
        val seedBoards = SeedBoardsData.boards
        seedBoards.forEach { seedBoard ->
            val dbBoard = boardRepository.getBoardById(seedBoard.id)
            val seedSymbolIds = seedBoard.symbols.map { it.id }
            if (dbBoard != null) {
                val dbBoardWithSymbols = boardRepository.getBoardWithSymbols(seedBoard.id)
                val existingSymbolIds = dbBoardWithSymbols?.symbols?.map { it.id }?.toSet() ?: emptySet()
                val missingSymbolIds = seedSymbolIds.filterNot(existingSymbolIds::contains)
                if (missingSymbolIds.isNotEmpty()) {
                    boardRepository.addSymbolsToBoard(
                        boardId = seedBoard.id,
                        symbolIds = missingSymbolIds,
                        startPosition = dbBoardWithSymbols?.symbols?.size ?: 0
                    )
                }
            } else {
                boardRepository.saveBoard(seedBoard)
                boardRepository.addSymbolsToBoard(seedBoard.id, seedSymbolIds, startPosition = 0)
            }
        }
    }

    private suspend fun syncVerifiedSymbolsFromSeed() {
        val currentDbSymbols = symbolRepository.getAllSymbolsOnce().associateBy { it.id }

        SeedSymbolsData.symbols.forEach { seed ->
            val current = currentDbSymbols[seed.id]
            val changed = current == null ||
                current.label != seed.label ||
                current.spokenText != seed.spokenText ||
                current.imagePath != seed.imagePath ||
                current.imageUrl != seed.imageUrl ||
                current.categoryId != seed.categoryId ||
                current.isEmergency != seed.isEmergency

            if (changed) {
                symbolRepository.upsertSeedSymbol(seed)
            }
        }
    }

    private suspend fun seedSymbols() {
        symbolRepository.saveSymbols(SeedSymbolsData.symbols)
    }

    private suspend fun seedBoards() {
        val boards = SeedBoardsData.boards
        boards.forEach { board ->
            boardRepository.saveBoard(board)
            boardRepository.addSymbolsToBoard(board.id, board.symbols.map { it.id }, startPosition = 0)
        }
    }

    private suspend fun seedRoutines() {
        val routines = SeedRoutinesData.routines.map { seed ->
            br.com.falacomigo.core.model.RoutineUiModel(
                id = seed.id,
                title = seed.title,
                subtitle = seed.subtitle,
                boardId = seed.boardId,
                isEmergency = seed.isEmergency,
                order = seed.order,
                symbols = seed.symbols,
                spokenText = seed.spokenText
            )
        }
        routineRepository.saveRoutines(routines)
    }
}
