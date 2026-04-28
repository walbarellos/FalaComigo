package br.com.falacomigo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.falacomigo.data.local.entities.SymbolEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SymbolDao {
    @Query("SELECT * FROM symbols ORDER BY labelPt ASC")
    fun getAllSymbols(): Flow<List<SymbolEntity>>

    @Query("SELECT * FROM symbols ORDER BY labelPt ASC")
    suspend fun getAllSymbolsOnce(): List<SymbolEntity>

    @Query("SELECT * FROM symbols WHERE id = :id")
    suspend fun getSymbolById(id: String): SymbolEntity?

    @Query("SELECT * FROM symbols WHERE category = :category ORDER BY labelPt ASC")
    fun getSymbolsByCategory(category: String): Flow<List<SymbolEntity>>

    @Query("SELECT * FROM symbols WHERE id IN (:ids)")
    fun getSymbolsByIds(ids: List<String>): Flow<List<SymbolEntity>>

    @Query("SELECT * FROM symbols WHERE labelPt LIKE '%' || :query || '%'")
    fun searchSymbols(query: String): Flow<List<SymbolEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymbol(symbol: SymbolEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymbols(symbols: List<SymbolEntity>)

    @Update
    suspend fun updateSymbol(symbol: SymbolEntity)

    @Query("DELETE FROM symbols WHERE id = :id")
    suspend fun deleteSymbol(id: String)

    @Query("SELECT * FROM symbols ORDER BY lastUsedAt DESC LIMIT 20")
    fun getRecentlyUsed(): Flow<List<SymbolEntity>>

    @Query("UPDATE symbols SET lastUsedAt = :timestamp WHERE id = :symbolId")
    suspend fun updateUsage(symbolId: String, timestamp: Long)

    @Query("SELECT COUNT(*) FROM symbols")
    suspend fun getSymbolCount(): Int

    @Query("SELECT * FROM symbols WHERE isCustom = 1 ORDER BY labelPt ASC")
    fun getCustomSymbols(): Flow<List<SymbolEntity>>

    @Query("UPDATE symbols SET localImagePath = :localImagePath, thumbnailPath = :thumbnailPath, imageDownloadStatus = 'READY', cachedAt = :cachedAt WHERE id = :symbolId")
    suspend fun markImageReady(symbolId: String, localImagePath: String, thumbnailPath: String?, cachedAt: Long)

    @Query("UPDATE symbols SET imageDownloadStatus = :status WHERE id = :symbolId")
    suspend fun updateImageDownloadStatus(symbolId: String, status: String)
}