package br.com.falacomigo.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import br.com.falacomigo.core.designsystem.tokens.ColorTokens

private val HighContrastColorScheme = lightColorScheme(
    primary = Color(0xFF000000),
    onPrimary = Color(0xFFFFFF00),
    primaryContainer = Color(0xFF1A1A1A),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFF000000),
    onSecondary = Color(0xFFFFFF00),
    secondaryContainer = Color(0xFF333333),
    onSecondaryContainer = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF000000),
    surfaceVariant = Color(0xFFFFFFFF),
    onSurfaceVariant = Color(0xFF000000),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF000000),
    error = Color(0xFFCC0000),
    onError = Color(0xFFFFFFFF),
    outline = Color(0xFF000000)
)

@Composable
fun HighContrastTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalHighContrastColors provides HighContrastColorScheme,
        content = content
    )
}

val LocalHighContrastColors = staticCompositionLocalOf { HighContrastColorScheme }

@Composable
fun highContrastColors() = LocalHighContrastColors.current

private val CalmDarkColorScheme = darkColorScheme(
    primary = Color(0xFFA8B4FF),
    onPrimary = Color(0xFF0F175C),
    primaryContainer = Color(0xFF2A3780),
    onPrimaryContainer = Color(0xFFDEE3FF),
    secondary = Color(0xFFF2A541),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF4A3500),
    onSecondaryContainer = Color(0xFFFFE0B2),
    surface = Color(0xFF121212),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF1E1E2E),
    onSurfaceVariant = Color(0xFFCACACA),
    background = Color(0xFF0D0D0D),
    onBackground = Color(0xFFE0E0E0),
    error = Color(0xFFCF6679),
    onError = Color(0xFF000000),
    outline = Color(0xFF757595)
)

@Composable
fun CalmDarkTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalCalmDarkColors provides CalmDarkColorScheme,
        content = content
    )
}

val LocalCalmDarkColors = staticCompositionLocalOf { CalmDarkColorScheme }

@Composable
fun calmDarkColors() = LocalCalmDarkColors.current