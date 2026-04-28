package br.com.falacomigo.feature.communication.domain

import br.com.falacomigo.core.model.RoutineUiModel
import br.com.falacomigo.core.model.SymbolUiModel
import br.com.falacomigo.data.repository.BoardRepository
import br.com.falacomigo.data.repository.RoutineRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Caso de Uso: Persiste a reordenação de símbolos em pranchas ou rotinas.
 * Garante Coesão e isola a lógica de persistência do domínio de UI.
 */
class MoveSymbolUseCase @Inject constructor(
    private val boardRepository: BoardRepository,
    private val routineRepository: RoutineRepository
) {
    suspend operator fun invoke(
        boardId: String,
        allRoutines: List<RoutineUiModel>,
        reorderedSymbols: List<SymbolUiModel>
    ) = withContext(Dispatchers.IO) {
        if (boardId.startsWith("routine_")) {
            val routineId = boardId.removePrefix("routine_")
            val routine = allRoutines.find { it.id == routineId }
            if (routine != null) {
                routineRepository.saveRoutine(
                    routine.copy(symbols = reorderedSymbols.map { it.id })
                )
            }
        } else if (boardId == "comunicacao") {
            boardRepository.reorderBoardSymbols(boardId, reorderedSymbols)
        }
    }
}
