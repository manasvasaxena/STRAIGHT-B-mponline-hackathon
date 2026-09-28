package com.learnquest.mp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = SaffronPrimary,
    secondary = ForestGreen,
    tertiary = StreakShieldBlue,
    background = BackgroundLight,
    surface = SurfaceCard,
    onPrimary = SurfaceCard,
    onSecondary = SurfaceCard,
    onBackground = TextDark,
    onSurface = TextDark
)

@Composable
fun LearnQuestTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
