package br.com.falacomigo.feature.communication.domain

import br.com.falacomigo.core.model.SymbolCategory
import br.com.falacomigo.core.model.SymbolUiModel
import br.com.falacomigo.data.remote.ArasaacApi
import br.com.falacomigo.data.repository.SymbolRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Caso de Uso: Orquestra a busca de símbolos em múltiplas fontes (Local e Remota).
 * Aplica o padrão de Pipeline de dados.
 */
class SearchSymbolsUseCase @Inject constructor(
    private val symbolRepository: SymbolRepository,
    private val arasaacApi: ArasaacApi
) {
    suspend operator fun invoke(query: String): List<SymbolUiModel> = withContext(Dispatchers.IO) {
        if (query.length < 2) return@withContext emptyList()

        // 1. Busca Local (Alta Performance)
        val localResults = symbolRepository.getAllSymbolsOnce().filter { 
            it.label.contains(query, ignoreCase = true) 
        }

        // 2. Busca Remota (Extensibilidade)
        val remoteResults = try {
            val response = arasaacApi.bestSearch(text = query)
            response.map { picto ->
                SymbolUiModel(
                    id = picto.id.toString(),
                    label = query.replaceFirstChar { it.uppercase() },
                    spokenText = query,
                    imageUrl = picto.imageUrl,
                    categoryId = "custom",
                    isCustom = true
                )
            }
        } catch (e: Exception) {
            emptyList()
        }

        // 3. Merge e Deduplicação (Determinismo)
        (localResults + remoteResults).distinctBy { it.id }
    }
}
