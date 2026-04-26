package br.com.falacomigo.core.model

data class SymbolUiModel(
    val id: String,
    val label: String,
    val spokenText: String,
    val imagePath: String? = null,
    val imageUrl: String? = null,
    val category: String = "general",
    val isEmergency: Boolean = false,
    val isCustom: Boolean = false,
    val accessibilityLabel: String = ""
) {
    init {
        if (accessibilityLabel.isEmpty()) {
            println("Warning: SymbolUiModel '$label' should have accessibilityLabel")
        }
    }
}