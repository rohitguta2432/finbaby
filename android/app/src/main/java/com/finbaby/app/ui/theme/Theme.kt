package com.finbaby.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    surface = Surface,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,
    surfaceContainerLowest = SurfaceContainerLowest,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
    inverseSurface = InverseSurface,
    inverseOnSurface = InverseOnSurface,
    outline = Outline,
    outlineVariant = OutlineVariant,
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryFixedDim,
    onPrimary = Color(0xFF003730),
    primaryContainer = Primary,
    onPrimaryContainer = PrimaryFixed,
    secondary = Color(0xFF84D5C5),
    onSecondary = Color(0xFF003830),
    secondaryContainer = Secondary,
    onSecondaryContainer = SecondaryContainer,
    tertiary = TertiaryFixedDim,
    onTertiary = Color(0xFF422C00),
    tertiaryContainer = Tertiary,
    onTertiaryContainer = TertiaryFixed,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Error,
    onErrorContainer = ErrorContainer,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
    inverseSurface = OnSurfaceDark,
    inverseOnSurface = SurfaceDark,
    outline = Color(0xFF87938F),
    outlineVariant = Color(0xFF3D4946),
)

@Composable
fun FinBabyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FinBabyTypography,
        content = content
    )
}
