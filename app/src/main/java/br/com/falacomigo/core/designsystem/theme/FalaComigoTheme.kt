package br.com.falacomigo.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import br.com.falacomigo.core.designsystem.tokens.ColorTokens
import br.com.falacomigo.core.designsystem.tokens.TypeTokens

private val LightColorScheme = lightColorScheme(
    primary = ColorTokens.Primary,
    onPrimary = ColorTokens.OnPrimary,
    primaryContainer = ColorTokens.PrimaryContainer,
    onPrimaryContainer = ColorTokens.OnPrimaryContainer,
    secondary = ColorTokens.Secondary,
    onSecondary = ColorTokens.OnSecondary,
    secondaryContainer = ColorTokens.SecondaryContainer,
    onSecondaryContainer = ColorTokens.OnSecondaryContainer,
    surface = ColorTokens.Surface,
    onSurface = ColorTokens.OnSurface,
    surfaceVariant = ColorTokens.SurfaceVariant,
    onSurfaceVariant = ColorTokens.OnSurfaceVariant,
    background = ColorTokens.Background,
    onBackground = ColorTokens.OnBackground,
    error = ColorTokens.Error,
    onError = ColorTokens.OnError,
    errorContainer = ColorTokens.ErrorContainer,
    onErrorContainer = ColorTokens.OnErrorContainer,
    outline = ColorTokens.Outline,
    outlineVariant = ColorTokens.OutlineVariant
)

private val DarkColorScheme = darkColorScheme(
    primary = ColorTokens.Dark.Primary,
    onPrimary = ColorTokens.Dark.OnPrimary,
    primaryContainer = ColorTokens.Dark.PrimaryContainer,
    onPrimaryContainer = ColorTokens.Dark.OnPrimaryContainer,
    surface = ColorTokens.Dark.Surface,
    onSurface = ColorTokens.Dark.OnSurface,
    background = ColorTokens.Dark.Background,
    onBackground = ColorTokens.Dark.OnSurface,
    error = ColorTokens.Error,
    onError = ColorTokens.OnError
)

@Composable
fun FalaComigoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    highContrast: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        highContrast -> LightColorScheme.copy(
            primary = ColorTokens.HighContrast.Primary,
            onPrimary = ColorTokens.HighContrast.OnPrimary,
            surface = ColorTokens.HighContrast.Surface,
            onSurface = ColorTokens.HighContrast.OnSurface
        )
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TypeTokens.toMaterialTypography,
        content = content
    )
}