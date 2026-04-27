package br.com.falacomigo.data.seed

import br.com.falacomigo.core.model.SymbolUiModel
import android.util.Log
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
        val allSymbols = symbolRepository.getAllSymbolsOnce()
        allSymbols.filter { it.label.lowercase() in listOf("frio", "casa", "desculpe", "licença") }.forEach {
            Log.d("FALACOMIGO_AUDIT", "Símbolo: ${it.label}, ID: ${it.id}, URL: ${it.imageUrl}, Path: ${it.imagePath}")
        }

        val symbolCount = symbolRepository.getSymbolCount()
        
        if (symbolCount == 0) {
            seedSymbols()
            seedBoards()
            seedRoutines()
        }
        
        // CORREÇÃO DEFINITIVA: IDs travados no SeedSymbols.kt agora são usados para atualizar o banco.
        // Removemos a busca dinâmica que estava trazendo grampeadores e sorvetes.
        syncVerifiedSymbolsFromSeed()
        syncSeedBoardsSymbols()
    }

    private suspend fun syncSeedBoardsSymbols() {
        val seedBoards = SeedBoardsData.boards
        seedBoards.forEach { seedBoard ->
            val dbBoard = boardRepository.getBoardById(seedBoard.id)
            if (dbBoard != null) {
                // The board exists. Ensure it has all the symbols it should have from the seed.
                val dbBoardWithSymbols = boardRepository.getBoardWithSymbols(seedBoard.id)
                val existingSymbolIds = dbBoardWithSymbols?.symbols?.map { it.id }?.toSet() ?: emptySet()
                
                seedBoard.symbols.forEachIndexed { index, symbol ->
                    if (!existingSymbolIds.contains(symbol.id)) {
                        // Append missing symbol to the board
                        val position = (dbBoardWithSymbols?.symbols?.size ?: 0) + index
                        boardRepository.addSymbolToBoard(seedBoard.id, symbol.id, position)
                    }
                }
            } else {
                // The board is completely missing in DB (maybe a new filter was added)
                boardRepository.saveBoard(seedBoard)
                seedBoard.symbols.forEachIndexed { index, symbol ->
                    boardRepository.addSymbolToBoard(seedBoard.id, symbol.id, index)
                }
            }
        }
    }

    private suspend fun syncVerifiedSymbolsFromSeed() {
        val allSeedSymbols = SeedSymbolsData.symbols
        val currentDbSymbols = symbolRepository.getAllSymbolsOnce()

        allSeedSymbols.forEach { seed ->
            val inDb = currentDbSymbols.find { it.id == seed.id }
            if (inDb != null) {
                // Atualiza o banco com os dados do Seed (Fonte da Verdade)
                // Isso garante que os IDs da ARASAAC e o spokenText fiquem certos
                symbolRepository.saveSymbol(seed)
            } else {
                symbolRepository.saveSymbol(seed)
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