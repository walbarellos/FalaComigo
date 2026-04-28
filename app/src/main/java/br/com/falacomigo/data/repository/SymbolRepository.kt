package br.com.falacomigo.data.repository

import android.content.Context
import br.com.falacomigo.core.model.SymbolCategory
import br.com.falacomigo.core.model.SymbolUiModel
import br.com.falacomigo.core.seed.SeedSymbols
import br.com.falacomigo.data.images.SymbolImageStore
import br.com.falacomigo.data.local.dao.SymbolDao
import br.com.falacomigo.data.local.entities.SymbolEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SymbolRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val symbolDao: SymbolDao,
    private val imageStore: SymbolImageStore
) {
    // Cache de IDs para evitar lookups repetidos
    private val idCache = mutableMapOf<String, Int>()

    private fun resolveResId(path: String?): Int {
        if (path.isNullOrEmpty()) return 0
        return idCache.getOrPut(path) {
            context.resources.getIdentifier(path, "drawable", context.packageName)
        }
    }

    fun getAllSymbols(): Flow<List<SymbolUiModel>> =
        symbolDao.getAllSymbols().map { entities -> entities.map { it.toUiModel() } }

    suspend fun getAllSymbolsOnce(): List<SymbolUiModel> =
        symbolDao.getAllSymbolsOnce().map { it.toUiModel() }

    fun getRecentlyUsed(): Flow<List<SymbolUiModel>> =
        symbolDao.getRecentlyUsed().map { entities -> entities.filter { it.lastUsedAt > 0 }.map { it.toUiModel() } }

    suspend fun updateUsage(id: String) {
        symbolDao.updateUsage(id, System.currentTimeMillis())
    }

    fun searchSymbols(query: String): Flow<List<SymbolUiModel>> =
        symbolDao.searchSymbols(query).map { entities -> entities.map { it.toUiModel() } }

    fun getSymbolsByIds(ids: Set<String>): Flow<List<SymbolUiModel>> =
        symbolDao.getSymbolsByIds(ids.toList()).map { entities -> entities.map { it.toUiModel() } }

    suspend fun getSymbolById(id: String): SymbolUiModel? =
        symbolDao.getSymbolById(id)?.toUiModel() ?: SeedSymbols.findById(id)

    suspend fun saveSymbol(symbol: SymbolUiModel) {
        val existing = symbolDao.getSymbolById(symbol.id)
        val merged = symbol.toEntity().copy(
            localImagePath = symbol.localImagePath ?: existing?.localImagePath,
            thumbnailPath = symbol.thumbnailPath ?: existing?.thumbnailPath,
            imageDownloadStatus = existing?.imageDownloadStatus 
                ?: if (symbol.imageUrl.isNullOrBlank()) "READY" else "PENDING",
            cachedAt = existing?.cachedAt,
            lastUsedAt = existing?.lastUsedAt ?: symbol.lastUsedAt ?: System.currentTimeMillis()
        )
        symbolDao.insertSymbol(merged)
    }

    suspend fun upsertSeedSymbol(symbol: SymbolUiModel) {
        val existing = symbolDao.getSymbolById(symbol.id)
        val merged = symbol.toEntity().copy(
            localImagePath = existing?.localImagePath,
            thumbnailPath = existing?.thumbnailPath,
            imageDownloadStatus = existing?.imageDownloadStatus
                ?: if (symbol.imageUrl.isNullOrBlank()) "READY" else "PENDING",
            cachedAt = existing?.cachedAt,
            lastUsedAt = existing?.lastUsedAt ?: 0L
        )
        if (existing != merged) {
            symbolDao.insertSymbol(merged)
        }
    }

    suspend fun markLocalImageReady(symbolId: String, fullPath: String, thumbPath: String?) {
        symbolDao.markImageReady(
            symbolId = symbolId,
            localImagePath = fullPath,
            thumbnailPath = thumbPath,
            cachedAt = System.currentTimeMillis()
        )
    }

    suspend fun markImageFailed(symbolId: String) {
        symbolDao.updateImageDownloadStatus(symbolId, "FAILED")
    }

    suspend fun saveSymbols(symbols: List<SymbolUiModel>) {
        symbolDao.insertSymbols(symbols.map { it.toEntity() })
    }

    suspend fun deleteSymbol(id: String) {
        symbolDao.deleteSymbol(id)
    }

    suspend fun getSymbolCount(): Int = symbolDao.getSymbolCount()

    private fun SymbolEntity.toUiModel(): SymbolUiModel {
        val localFile = imageStore.getLocalFile(id)
        val thumbFile = imageStore.getThumbnailFile(id)
        return SymbolUiModel(
            id = id,
            label = labelPt,
            spokenText = spokenText,
            imagePath = imagePath,
            imageUrl = imageUrl,
            localImagePath = localImagePath ?: localFile?.absolutePath,
            thumbnailPath = thumbnailPath ?: thumbFile?.absolutePath,
            imageDownloadStatus = imageDownloadStatus,
            categoryId = category,
            isCustom = isCustom,
            isEmergency = isEmergency,
            lastUsedAt = lastUsedAt,
            imageResId = resolveResId(imagePath)
        )
    }

    private fun SymbolUiModel.toEntity() = SymbolEntity(
        id = id,
        labelPt = label,
        spokenText = spokenText,
        imagePath = imagePath,
        imageUrl = imageUrl,
        localImagePath = localImagePath,
        thumbnailPath = thumbnailPath,
        imageDownloadStatus = imageDownloadStatus,
        category = categoryId,
        isCustom = isCustom,
        isEmergency = isEmergency,
        lastUsedAt = lastUsedAt ?: 0L
    )
}
