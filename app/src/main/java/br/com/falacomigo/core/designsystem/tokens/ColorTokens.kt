package br.com.falacomigo.core.designsystem.tokens

import androidx.compose.ui.graphics.Color

object ColorTokens {
    // Primary colors
    val Primary = Color(0xFF2F6FDB)
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFFE8F1FF)
    val OnPrimaryContainer = Color(0xFF001D36)

    // Surface & Background (ChatGPT spec)
    val Background = Color(0xFFF8FAFC)  // azul muito claro.clean
    val Surface = Color(0xFFFFFFFF)
    val OnSurface = Color(0xFF111827)    // almost black
    val OnBackground = Color(0xFF111827)
    val SurfaceVariant = Color(0xFFF3F4F6)
    val OnSurfaceVariant = Color(0xFF6B7280)

    // Secondary
    val Secondary = Color(0xFFF59E0B)
    val OnSecondary = Color(0xFFFFFFFF)
    val SecondaryContainer = Color(0xFFFFF4CF)
    val OnSecondaryContainer = Color(0xFF3D2000)

    // Error
    val Error = Color(0xFFDC2626)
    val OnError = Color(0xFFFFFFFF)
    val ErrorContainer = Color(0xFFFEE2E2)
    val OnErrorContainer = Color(0xFF7F1D1D)

    // Borders
    val Outline = Color(0xFFE5E7EB)
    val OutlineVariant = Color(0xFFF3F4F6)

    // Card colors - pastels ChatGPT (nunca brighter)
    val CardMint = Color(0xFFEAF7EF)
    val CardYellow = Color(0xFFFFF4CF)
    val CardBlue = Color(0xFFE5F4FF)
    val CardPurple = Color(0xFFF0EAFB)
    val CardPeach = Color(0xFFFFE9D9)
    val CardPink = Color(0xFFFCE7F3)
    val CardOrange = Color(0xFFFFEDD5) // Laranja suave para numerais
    val CardGray = Color(0xFFF3F4F6)

    // Map category to card color
    fun getCardColor(category: String): Color {
        return when (category.lowercase()) {
            "basic" -> CardMint
            "emocoes" -> CardPurple
            "necessidades" -> CardYellow
            "saude" -> CardPink
            "emergencia" -> CardPink
            "sensorial" -> CardPeach
            "social" -> CardBlue
            "numeral" -> CardOrange
            "alimentacao" -> Color(0xFFFEF3C7) // Amarelo Mel
            "brincar" -> Color(0xFFE0E7FF) // Indigo suave
            "lugares" -> Color(0xFFF3E8FF) // Roxo claro
            "acoes" -> Color(0xFFDCFCE7) // Verde claro
            "familia" -> Color(0xFFE2E8F0) // Slate suave
            "higiene" -> Color(0xFFCCFBF1) // Ciano água
            "clima" -> Color(0xFFFEF9C3) // Amarelo sol claro
            else -> CardGray
        }
    }

    // Existing symbol colors
    val SymbolCardBackground = Surface
    val SymbolCardBorder = Outline
    val FocusBorder = Primary
    val FocusHighlight = Color(0xFF10B981) // Verde esmeralda suave para elocução
    val PressedBackground = PrimaryContainer

    object HighContrast {
        val Primary = Color(0xFF000000)
        val OnPrimary = Color(0xFFFFFF00)
        val Surface = Color(0xFFFFFFFF)
        val OnSurface = Color(0xFF000000)
        val SymbolCardBorder = Color(0xFF000000)
    }

    object Dark {
        val Primary = Color(0xFF60A5FA)
        val OnPrimary = Color(0xFF001D36)
        val PrimaryContainer = Color(0xFF1E40AF)
        val OnPrimaryContainer = Color(0xFFDBEAFE)
        val Surface = Color(0xFF1F2937)
        val OnSurface = Color(0xFFF9FAFB)
        val Background = Color(0xFF111827)
        val SymbolCardBackground = Color(0xFF374151)
    }
}