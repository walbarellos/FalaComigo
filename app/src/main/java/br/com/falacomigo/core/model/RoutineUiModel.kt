package br.com.falacomigo.core.model

data class RoutineUiModel(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val spokenText: String = "",
    val symbols: List<String> = emptyList(),
    val iconName: String = "history",
    val imageUrl: String? = null,
    val imagePath: String? = null,
    val boardId: String,
    val isEmergency: Boolean = false,
    val order: Int = 0
)