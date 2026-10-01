package com.learnquest.mp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.learnquest.mp.data.model.DailyChallenge
import com.learnquest.mp.model.Language
import com.learnquest.mp.ui.appStrings

/** Teaser card for the daily challenge (the question itself is shown later). */
@Composable
fun DailyChallengeCard(
    challenge: DailyChallenge,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
    appLanguage: Language = Language.ENGLISH
) {
    val strings = appLanguage.appStrings()
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(strings.t("🧩 Daily Challenge", "🧩 दैनिक चुनौती", "🧩 Daily Challenge"), style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                ) {
                    Text("+${challenge.rewardXp} XP",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge)
                }
            }
            Text(strings.t("Today's challenge is waiting!", "आज की चुनौती आपका इंतज़ार कर रही है!", "Today's challenge ready hai!"), style = MaterialTheme.typography.bodyLarge)
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                )
            ) { Text(strings.t("Start Challenge", "चुनौती शुरू करें", "Challenge Start करें"), style = MaterialTheme.typography.titleMedium) }
        }
    }
}
