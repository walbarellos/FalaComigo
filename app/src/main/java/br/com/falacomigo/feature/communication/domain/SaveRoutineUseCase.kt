package br.com.falacomigo.feature.communication.domain

import br.com.falacomigo.core.model.RoutineUiModel
import br.com.falacomigo.core.model.SymbolUiModel
import br.com.falacomigo.data.repository.RoutineRepository
import br.com.falacomigo.data.repository.SymbolRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Caso de Uso: Salva ou atualiza uma rotina e garante a persistência dos símbolos customizados.
 */
class SaveRoutineUseCase @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val symbolRepository: SymbolRepository
) {
    suspend operator fun invoke(
        editingRoutine: RoutineUiModel?,
        name: String,
        symbols: List<SymbolUiModel>
    ) = withContext(Dispatchers.IO) {
        // 1. Persiste símbolos (se forem novos/custom)
        symbols.forEach { 
            symbolRepository.saveSymbol(it.copy(isCustom = true)) 
        }

        // 2. Cria ou atualiza a rotina
        val id = editingRoutine?.id ?: System.currentTimeMillis().toString()
        val routine = RoutineUiModel(
            id = id,
            title = name,
            spokenText = "",
            symbols = symbols.map { it.id },
            boardId = id
        )
        
        routineRepository.saveRoutine(routine)
    }
}
