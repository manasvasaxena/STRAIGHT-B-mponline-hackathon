package com.learnquest.mp.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = DeepBlue,
    onPrimary = Color.White,
    primaryContainer = DeepBlueContainer,
    onPrimaryContainer = OnDeepBlueContainer,
    secondary = LearnGreen,
    onSecondary = Color.White,
    secondaryContainer = LearnGreenContainer,
    onSecondaryContainer = OnLearnGreenContainer,
    tertiary = WarmOrange,
    onTertiary = Color.White,
    tertiaryContainer = WarmYellowContainer,
    onTertiaryContainer = OnWarmYellowContainer,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
)

private val DarkColors = darkColorScheme(
    primary = DarkPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DeepBlueContainer,
    secondary = DarkSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = LearnGreenContainer,
    tertiary = DarkTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = WarmYellowContainer,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
)

@Composable
fun LearnQuestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
