package br.com.falacomigo.data.local.dao

import androidx.room.*
import br.com.falacomigo.data.local.entities.CachedPictogram
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedPictogramDao {
    @Query("SELECT * FROM cached_pictograms ORDER BY savedAt DESC")
    fun getAll(): Flow<List<CachedPictogram>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(p: CachedPictogram)

    @Delete
    suspend fun delete(p: CachedPictogram)

    @Query("SELECT * FROM cached_pictograms WHERE label = :label LIMIT 1")
    suspend fun findByLabel(label: String): CachedPictogram?

    @Query("SELECT * FROM cached_pictograms WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): CachedPictogram?

    @Query("SELECT * FROM cached_pictograms WHERE label LIKE '%' || :query || '%'")
    suspend fun searchLocal(query: String): List<CachedPictogram>
}