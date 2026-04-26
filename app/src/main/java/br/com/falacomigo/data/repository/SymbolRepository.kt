package br.com.falacomigo.data.repository

import br.com.falacomigo.core.model.SymbolUiModel
import br.com.falacomigo.data.local.dao.SymbolDao
import br.com.falacomigo.data.local.entities.SymbolEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SymbolRepository @Inject constructor(
    private val symbolDao: SymbolDao
) {
    fun getAllSymbols(): Flow<List<SymbolUiModel>> {
        return symbolDao.getAllSymbols().map { entities ->
            entities.map { it.toUiModel() }
        }
    }

    fun getCustomSymbols(): Flow<List<SymbolUiModel>> {
        return symbolDao.getCustomSymbols().map { entities ->
            entities.map { it.toUiModel() }
        }
    }

    fun searchSymbols(query: String): Flow<List<SymbolUiModel>> {
        return symbolDao.searchSymbols(query).map { entities ->
            entities.map { it.toUiModel() }
        }
    }

    suspend fun getSymbolById(id: String): SymbolUiModel? {
        return symbolDao.getSymbolById(id)?.toUiModel()
    }

    suspend fun saveSymbol(symbol: SymbolUiModel) {
        symbolDao.insertSymbol(symbol.toEntity())
    }

    suspend fun saveSymbols(symbols: List<SymbolUiModel>) {
        symbolDao.insertSymbols(symbols.map { it.toEntity() })
    }

    suspend fun deleteSymbol(id: String) {
        symbolDao.deleteSymbol(id)
    }

    suspend fun getSymbolCount(): Int {
        return symbolDao.getSymbolCount()
    }

    private fun SymbolEntity.toUiModel(): SymbolUiModel {
        return SymbolUiModel(
            id = id,
            label = labelPt,
            spokenText = spokenText,
            imagePath = imagePath,
            category = category,
            isCustom = isCustom,
            accessibilityLabel = "$labelPt, símbolo"
        )
    }

    private fun SymbolUiModel.toEntity(): SymbolEntity {
        return SymbolEntity(
            id = id,
            labelPt = label,
            spokenText = spokenText,
            imagePath = imagePath,
            category = category,
            isCustom = isCustom
        )
    }
}