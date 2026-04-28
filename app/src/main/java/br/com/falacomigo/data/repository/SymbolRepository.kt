package br.com.falacomigo.data.repository

import android.content.Context
import br.com.falacomigo.core.model.SymbolCategory
import br.com.falacomigo.core.model.SymbolUiModel
import br.com.falacomigo.core.seed.SeedSymbols
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
    private val symbolDao: SymbolDao
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
        val entity = symbol.toEntity().copy(lastUsedAt = System.currentTimeMillis())
        symbolDao.insertSymbol(entity)
    }

    suspend fun saveSymbols(symbols: List<SymbolUiModel>) {
        symbolDao.insertSymbols(symbols.map { it.toEntity() })
    }

    suspend fun deleteSymbol(id: String) {
        symbolDao.deleteSymbol(id)
    }

    suspend fun getSymbolCount(): Int = symbolDao.getSymbolCount()

    private fun SymbolEntity.toUiModel() = SymbolUiModel(
        id = id, label = labelPt, spokenText = spokenText, imagePath = imagePath,
        imageUrl = imageUrl, categoryId = category,
        isCustom = isCustom, lastUsedAt = lastUsedAt,
        imageResId = resolveResId(imagePath)
    )

    private fun SymbolUiModel.toEntity() = SymbolEntity(
        id = id, labelPt = label, spokenText = spokenText, imagePath = imagePath,
        imageUrl = imageUrl, category = categoryId,
        isCustom = isCustom, lastUsedAt = lastUsedAt ?: 0L
    )
}