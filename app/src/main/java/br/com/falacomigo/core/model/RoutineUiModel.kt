package br.com.falacomigo.core.model

data class RoutineUiModel(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val iconName: String = "home",
    val boardId: String,
    val isEmergency: Boolean = false,
    val order: Int = 0
)