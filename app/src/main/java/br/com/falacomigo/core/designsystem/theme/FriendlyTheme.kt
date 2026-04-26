package br.com.falacomigo.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import br.com.falacomigo.core.designsystem.tokens.ColorTokens

private val FriendlyColorScheme = lightColorScheme(
    primary = Color(0xFF6B7FD4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDEE3FF),
    onPrimaryContainer = Color(0xFF0A1572),
    secondary = Color(0xFFF2A541),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFFFFE0B2),
    onSecondaryContainer = Color(0xFF3E2000),
    surface = Color(0xFFFAFAFA),
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFF0F1FA),
    onSurfaceVariant = Color(0xFF3A3D59),
    background = Color(0xFFF5F5F7),
    onBackground = Color(0xFF1A1A1A),
    error = Color(0xFFB00020),
    onError = Color(0xFFFFFFFF),
    outline = Color(0xFF757595)
)

@Composable
fun FriendlyTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalFriendlyColors provides FriendlyColorScheme,
        content = content
    )
}

val LocalFriendlyColors = staticCompositionLocalOf { FriendlyColorScheme }

@Composable
fun friendlyColors() = LocalFriendlyColors.current