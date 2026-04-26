package br.com.falacomigo.core.model

data class BoardUiModel(
    val id: String,
    val title: String,
    val description: String = "",
    val symbols: List<SymbolUiModel> = emptyList(),
    val columns: Int = 3,
    val isEmergency: Boolean = false,
    val isDefault: Boolean = false,
    val order: Int = 0
)