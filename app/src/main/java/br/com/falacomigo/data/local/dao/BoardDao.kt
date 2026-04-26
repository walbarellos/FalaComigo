package br.com.falacomigo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import br.com.falacomigo.data.local.entities.BoardEntity
import br.com.falacomigo.data.local.entities.BoardSymbolEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BoardDao {
    @Query("SELECT * FROM boards ORDER BY displayOrder ASC")
    fun getAllBoards(): Flow<List<BoardEntity>>

    @Query("SELECT * FROM boards WHERE id = :id")
    suspend fun getBoardById(id: String): BoardEntity?

    @Query("SELECT * FROM boards WHERE isEmergency = 1 LIMIT 1")
    suspend fun getEmergencyBoard(): BoardEntity?

    @Query("SELECT * FROM boards WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultBoard(): BoardEntity?

    @Query("SELECT * FROM boards WHERE routineId = :routineId ORDER BY displayOrder ASC")
    fun getBoardsByRoutine(routineId: String): Flow<List<BoardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBoard(board: BoardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBoards(boards: List<BoardEntity>)

    @Query("DELETE FROM boards WHERE id = :id")
    suspend fun deleteBoard(id: String)

    @Query("SELECT COUNT(*) FROM boards")
    suspend fun getBoardCount(): Int

    @Query("SELECT bs.boardId, bs.symbolId, bs.position, bs.customLabel, bs.customSpokenText, bs.backgroundColor FROM board_symbols bs WHERE bs.boardId = :boardId ORDER BY bs.position ASC")
    fun getBoardSymbols(boardId: String): Flow<List<BoardSymbolEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBoardSymbol(boardSymbol: BoardSymbolEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBoardSymbols(boardSymbols: List<BoardSymbolEntity>)

    @Query("DELETE FROM board_symbols WHERE boardId = :boardId AND symbolId = :symbolId")
    suspend fun removeSymbolFromBoard(boardId: String, symbolId: String)

    @Query("DELETE FROM board_symbols WHERE boardId = :boardId")
    suspend fun clearBoardSymbols(boardId: String)

    @Transaction
    suspend fun reorderBoardSymbols(boardId: String, symbols: List<BoardSymbolEntity>) {
        clearBoardSymbols(boardId)
        insertBoardSymbols(symbols)
    }
}