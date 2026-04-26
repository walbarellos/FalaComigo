package br.com.falacomigo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.falacomigo.data.local.entities.RoutineEntity
import br.com.falacomigo.data.local.entities.RoutineBoardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines ORDER BY displayOrder ASC")
    fun getAllRoutines(): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM routines WHERE id = :id")
    suspend fun getRoutineById(id: String): RoutineEntity?

    @Query("SELECT * FROM routines WHERE isAuto = 1 ORDER BY displayOrder ASC")
    fun getAutoRoutines(): Flow<List<RoutineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutines(routines: List<RoutineEntity>)

    @Query("DELETE FROM routines WHERE id = :id")
    suspend fun deleteRoutine(id: String)

    @Query("SELECT rb.routineId, rb.boardId, rb.displayOrder FROM routine_boards rb WHERE rb.routineId = :routineId ORDER BY rb.displayOrder ASC")
    fun getRoutineBoards(routineId: String): Flow<List<RoutineBoardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineBoard(routineBoard: RoutineBoardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineBoards(routineBoards: List<RoutineBoardEntity>)

    @Query("DELETE FROM routine_boards WHERE routineId = :routineId AND boardId = :boardId")
    suspend fun removeBoardFromRoutine(routineId: String, boardId: String)
}