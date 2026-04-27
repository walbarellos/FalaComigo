package br.com.falacomigo.data.repository

import br.com.falacomigo.core.model.BoardUiModel
import br.com.falacomigo.core.model.SymbolUiModel
import br.com.falacomigo.data.local.dao.BoardDao
import br.com.falacomigo.data.local.entities.BoardEntity
import br.com.falacomigo.data.local.entities.BoardSymbolEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class BoardRepository @Inject constructor(
    private val boardDao: BoardDao,
    private val symbolRepository: SymbolRepository
) {

    fun getAllBoards(): Flow<List<BoardUiModel>> =
        boardDao.getAllBoards().map { list -> list.map { it.toUiModel() } }

    fun getBoardsByRoutine(routineId: String): Flow<List<BoardUiModel>> =
        boardDao.getBoardsByRoutine(routineId).map { list -> list.map { it.toUiModel() } }

    suspend fun getBoardById(id: String): BoardUiModel? =
        boardDao.getBoardById(id)?.toUiModel()

    /**
     * Flow reativo da prancha com seus símbolos ordenados por posição.
     *
     * OTIMIZAÇÃO vs versão anterior:
     * A versão anterior usava symbolRepository.getAllSymbols() no combine — isso fazia
     * o flow re-emitir a prancha inteira a cada atualização de QUALQUER símbolo no app.
     *
     * SOLUÇÃO: combine boardSymbols (vínculos) → extrair IDs → flatMapLatest para
     * getSymbolsByIds(ids). Agora só re-emite quando um símbolo DESTE board muda.
     *
     * Requer SymbolRepository.getSymbolsByIds(ids: Set<String>): Flow<List<SymbolUiModel>>
     * (implementação sugerida no SymbolRepository — query: SELECT * FROM symbols WHERE id IN (:ids))
     */
    fun getBoardWithSymbolsFlow(boardId: String): Flow<BoardUiModel?> {
        return combine(
            boardDao.getBoardByIdFlow(boardId),
            boardDao.getBoardSymbols(boardId)
        ) { entity, boardSymbols ->
            entity to boardSymbols
        }.flatMapLatest { (entity, boardSymbols) ->
            if (entity == null) return@flatMapLatest flowOf(null)

            val symbolIds = boardSymbols.map { it.symbolId }.toSet()

            if (symbolIds.isEmpty()) {
                return@flatMapLatest flowOf(entity.toUiModel())
            }

            // Observa APENAS os símbolos deste board — não o catálogo inteiro
            symbolRepository.getSymbolsByIds(symbolIds).map { symbols ->
                val symbolsById = symbols.associateBy { it.id }

                // Monta lista respeitando a posição definida no BoardSymbolEntity
                val orderedSymbols = boardSymbols
                    .sortedBy { it.position }
                    .mapNotNull { bs -> symbolsById[bs.symbolId] }

                entity.toUiModel().copy(symbols = orderedSymbols)
            }
        }
    }

    /**
     * Versão one-shot (suspend) para casos que não precisam de reatividade.
     * Usa .first() no flow de symbols para obter o estado atual sem subscription.
     */
    suspend fun getBoardWithSymbols(boardId: String): BoardUiModel? {
        val boardEntity = boardDao.getBoardById(boardId) ?: return null
        val boardSymbols = boardDao.getBoardSymbols(boardId).first()

        if (boardSymbols.isEmpty()) return boardEntity.toUiModel()

        val symbolIds = boardSymbols.map { it.symbolId }.toSet()
        val symbolsById = symbolRepository.getSymbolsByIds(symbolIds).first().associateBy { it.id }

        val orderedSymbols = boardSymbols
            .sortedBy { it.position }
            .mapNotNull { bs -> symbolsById[bs.symbolId] }

        return boardEntity.toUiModel().copy(symbols = orderedSymbols)
    }

    suspend fun getEmergencyBoard(): BoardUiModel? =
        boardDao.getEmergencyBoard()?.toUiModel()

    suspend fun getDefaultBoard(): BoardUiModel? =
        boardDao.getDefaultBoard()?.toUiModel()

    suspend fun saveBoard(board: BoardUiModel) =
        boardDao.insertBoard(board.toEntity())

    suspend fun saveBoards(boards: List<BoardUiModel>) =
        boardDao.insertBoards(boards.map { it.toEntity() })

    suspend fun deleteBoard(id: String) =
        boardDao.deleteBoard(id)

    suspend fun addSymbolToBoard(boardId: String, symbolId: String, position: Int) {
        boardDao.insertBoardSymbol(
            BoardSymbolEntity(boardId = boardId, symbolId = symbolId, position = position)
        )
    }

    suspend fun removeSymbolFromBoard(boardId: String, symbolId: String) =
        boardDao.removeSymbolFromBoard(boardId, symbolId)

    suspend fun reorderBoardSymbols(boardId: String, symbols: List<SymbolUiModel>) {
        val boardSymbols = symbols.mapIndexed { index, symbol ->
            BoardSymbolEntity(boardId = boardId, symbolId = symbol.id, position = index)
        }
        boardDao.reorderBoardSymbols(boardId, boardSymbols)
    }

    suspend fun getBoardCount(): Int = boardDao.getBoardCount()

    // ── Mappers ─────────────────────────────────────────────────────────────

    private fun BoardEntity.toUiModel() = BoardUiModel(
        id = id,
        title = name,
        description = description,
        isEmergency = isEmergency,
        isDefault = isDefault,
        columns = gridCols
    )

    private fun BoardUiModel.toEntity() = BoardEntity(
        id = id,
        name = title,
        description = description,
        gridCols = columns,
        isEmergency = isEmergency,
        isDefault = isDefault,
        isSeed = true
    )
}