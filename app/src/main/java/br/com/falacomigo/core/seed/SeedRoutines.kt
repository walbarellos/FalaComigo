package br.com.falacomigo.core.seed

import br.com.falacomigo.core.model.RoutineUiModel

object SeedRoutines {
    val routines: List<RoutineUiModel> = listOf(
        RoutineUiModel(
            id = "principal",
            title = "Principal",
            subtitle = "Comunicação do dia a dia",
            iconName = "chat",
            boardId = "principal",
            order = 0
        ),
        RoutineUiModel(
            id = "urgente",
            title = "Urgente",
            subtitle = "Quando precisa de ajuda",
            iconName = "warning",
            boardId = "urgente",
            isEmergency = true,
            order = 1
        ),
        RoutineUiModel(
            id = "necessidades",
            title = "Necessidades",
            subtitle = "Básico do cotidiano",
            iconName = "water_drop",
            boardId = "necessidades",
            order = 2
        ),
        RoutineUiModel(
            id = "emocoes",
            title = "Emoções",
            subtitle = "Como estou me sentindo",
            iconName = "mood",
            boardId = "emocoes",
            order = 3
        )
    )

    fun findById(id: String): RoutineUiModel? = routines.find { it.id == id }

    fun findByBoardId(boardId: String): RoutineUiModel? = routines.find { it.boardId == boardId }

    val defaultRoutine: RoutineUiModel get() = routines.first()

    val emergencyRoutine: RoutineUiModel get() = routines.first { it.isEmergency }
}