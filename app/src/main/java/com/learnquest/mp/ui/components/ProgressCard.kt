package com.learnquest.mp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.learnquest.mp.data.model.StudentProgress
import com.learnquest.mp.model.Language
import com.learnquest.mp.ui.appStrings

/** Prominent card: level, XP, progress bar and streak. */
@Composable
fun ProgressCard(
    progress: StudentProgress,
    modifier: Modifier = Modifier,
    appLanguage: Language = Language.ENGLISH
) {
    val strings = appLanguage.appStrings()
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(strings.level(progress.level), style = MaterialTheme.typography.headlineMedium)
            Text(
                "${progress.currentXp} / ${progress.xpForNextLevel} XP",
                style = MaterialTheme.typography.titleMedium
            )
            LinearProgressIndicator(
                progress = { progress.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = MaterialTheme.colorScheme.tertiaryContainer,
                trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)
            )
            // Percentage text so progress is not shown by the bar's color alone.
            Text(
                strings.progressToLevel((progress.progressFraction * 100).toInt(), progress.level + 1),
                style = MaterialTheme.typography.bodyMedium
            )
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            ) {
                Text(
                    strings.dayStreak(progress.streakDays),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
