package br.com.falacomigo.data.repository

import br.com.falacomigo.core.model.RoutineUiModel
import br.com.falacomigo.data.local.dao.RoutineDao
import br.com.falacomigo.data.local.entities.RoutineEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineRepository @Inject constructor(
    private val routineDao: RoutineDao
) {
    fun getAllRoutines(): Flow<List<RoutineUiModel>> {
        return routineDao.getAllRoutines().map { entities ->
            entities.map { it.toUiModel() }
        }
    }

    suspend fun getRoutineById(id: String): RoutineUiModel? {
        return routineDao.getRoutineById(id)?.toUiModel()
    }

    suspend fun getDefaultRoutine(): RoutineUiModel? {
        return routineDao.getRoutineById("principal")?.toUiModel()
    }

    suspend fun saveRoutine(routine: RoutineUiModel) {
        routineDao.insertRoutine(routine.toEntity())
    }

    suspend fun saveRoutines(routines: List<RoutineUiModel>) {
        routineDao.insertRoutines(routines.map { it.toEntity() })
    }

    suspend fun deleteRoutine(id: String) {
        routineDao.deleteRoutine(id)
    }

    private fun RoutineEntity.toUiModel(): RoutineUiModel {
        return RoutineUiModel(
            id = id,
            title = name,
            subtitle = "",
            iconName = icon,
            boardId = id,
            order = displayOrder
        )
    }

    private fun RoutineUiModel.toEntity(): RoutineEntity {
        return RoutineEntity(
            id = id,
            name = title,
            icon = iconName,
            displayOrder = order
        )
    }
}