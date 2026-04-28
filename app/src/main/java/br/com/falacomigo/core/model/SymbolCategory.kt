package br.com.falacomigo.core.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import br.com.falacomigo.core.designsystem.tokens.ColorTokens

sealed class SymbolCategory(
    val id: String,
    val title: String,
    val color: Color,
    val icon: ImageVector
) {
    object Numeral : SymbolCategory("numeral", "Números", ColorTokens.CategoryNumeral, Icons.Default.Tag)
    object Social : SymbolCategory("social", "Social", ColorTokens.CategorySocial, Icons.Default.Chat)
    object Alimentacao : SymbolCategory("alimentacao", "Alimentação", ColorTokens.CategoryFood, Icons.Default.FoodBank)
    object Atividades : SymbolCategory("atividades", "Atividades", ColorTokens.CategoryActivities, Icons.Default.SportsEsports)
    object Necessidades : SymbolCategory("necessidades", "Necessidades", ColorTokens.CategoryNeeds, Icons.Default.Soap)
    object Emocoes : SymbolCategory("emocoes", "Emoções", ColorTokens.CategoryEmotions, Icons.Default.Face)
    object Emergencia : SymbolCategory("emergencia", "Urgente", ColorTokens.CategoryEmergency, Icons.Default.Warning)
    data class Custom(val name: String) : SymbolCategory("custom", name, ColorTokens.SurfaceVariant, Icons.Default.GridView)
    object General : SymbolCategory("general", "Geral", ColorTokens.SurfaceVariant, Icons.Default.GridView)

    companion object {
        fun fromId(id: String): SymbolCategory = when(id) {
            "numeral" -> Numeral
            "social" -> Social
            "alimentacao" -> Alimentacao
            "atividades" -> Atividades
            "necessidades" -> Necessidades
            "emocoes" -> Emocoes
            "emergencia" -> Emergencia
            else -> if (id.startsWith("custom_")) Custom(id.removePrefix("custom_")) else General
        }
    }
}
