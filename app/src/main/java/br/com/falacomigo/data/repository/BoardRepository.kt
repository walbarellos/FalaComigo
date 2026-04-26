package br.com.falacomigo.data.repository

import br.com.falacomigo.core.model.BoardUiModel
import br.com.falacomigo.core.model.SymbolUiModel
import br.com.falacomigo.data.local.dao.BoardDao
import br.com.falacomigo.data.local.entities.BoardEntity
import br.com.falacomigo.data.local.entities.BoardSymbolEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BoardRepository @Inject constructor(
    private val boardDao: BoardDao,
    private val symbolRepository: SymbolRepository
) {
    fun getAllBoards(): Flow<List<BoardUiModel>> {
        return boardDao.getAllBoards().map { entities ->
            entities.map { it.toUiModel() }
        }
    }

    fun getBoardsByRoutine(routineId: String): Flow<List<BoardUiModel>> {
        return boardDao.getBoardsByRoutine(routineId).map { entities ->
            entities.map { it.toUiModel() }
        }
    }

    suspend fun getBoardById(id: String): BoardUiModel? {
        return boardDao.getBoardById(id)?.toUiModel()
    }

    suspend fun getBoardWithSymbols(boardId: String): BoardUiModel? {
        val board = boardDao.getBoardById(boardId) ?: return null
        return boardDao.getBoardSymbols(boardId).map { boardSymbols ->
            val symbols = boardSymbols.mapNotNull { bs ->
                symbolRepository.getSymbolById(bs.symbolId)
            }
            board.toUiModel().copy(symbols = symbols)
        }.let { board.toUiModel() }
    }

    suspend fun getEmergencyBoard(): BoardUiModel? {
        return boardDao.getEmergencyBoard()?.toUiModel()
    }

    suspend fun getDefaultBoard(): BoardUiModel? {
        return boardDao.getDefaultBoard()?.toUiModel()
    }

    suspend fun saveBoard(board: BoardUiModel) {
        boardDao.insertBoard(board.toEntity())
    }

    suspend fun saveBoards(boards: List<BoardUiModel>) {
        boardDao.insertBoards(boards.map { it.toEntity() })
    }

    suspend fun deleteBoard(id: String) {
        boardDao.deleteBoard(id)
    }

    suspend fun addSymbolToBoard(boardId: String, symbolId: String, position: Int) {
        boardDao.insertBoardSymbol(
            BoardSymbolEntity(
                boardId = boardId,
                symbolId = symbolId,
                position = position
            )
        )
    }

    suspend fun removeSymbolFromBoard(boardId: String, symbolId: String) {
        boardDao.removeSymbolFromBoard(boardId, symbolId)
    }

    suspend fun reorderBoardSymbols(boardId: String, symbols: List<SymbolUiModel>) {
        val boardSymbols = symbols.mapIndexed { index, symbol ->
            BoardSymbolEntity(
                boardId = boardId,
                symbolId = symbol.id,
                position = index
            )
        }
        boardDao.reorderBoardSymbols(boardId, boardSymbols)
    }

    suspend fun getBoardCount(): Int {
        return boardDao.getBoardCount()
    }

    private fun BoardEntity.toUiModel(): BoardUiModel {
        return BoardUiModel(
            id = id,
            title = name,
            description = description,
            isEmergency = isEmergency,
            isDefault = isDefault,
            columns = gridCols
        )
    }

    private fun BoardUiModel.toEntity(): BoardEntity {
        return BoardEntity(
            id = id,
            name = title,
            description = description,
            gridCols = columns,
            isEmergency = isEmergency,
            isDefault = isDefault,
            isSeed = true
        )
    }
}