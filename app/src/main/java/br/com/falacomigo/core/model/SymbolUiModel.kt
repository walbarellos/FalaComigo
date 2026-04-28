package br.com.falacomigo.core.model

import androidx.compose.runtime.Immutable

@Immutable
data class SymbolUiModel(
    val id: String,
    val label: String,
    val spokenText: String,
    val imagePath: String? = null,
    val imageUrl: String? = null,
    val localImagePath: String? = null,
    val thumbnailPath: String? = null,
    val imageDownloadStatus: String = "PENDING",
    val imageResId: Int = 0,
    val categoryId: String = "general", // Volta para String para compatibilidade com o banco
    val isEmergency: Boolean = false,
    val isCustom: Boolean = false,
    val lastUsedAt: Long? = null,
    val accessibilityLabel: String = ""
) {
    // Computed property: Resolve o objeto de domínio sob demanda (O(1))
    val category: SymbolCategory
        get() = SymbolCategory.fromId(categoryId)

    init {
        if (accessibilityLabel.isEmpty()) {
            // Log silenciado
        }
    }
}
