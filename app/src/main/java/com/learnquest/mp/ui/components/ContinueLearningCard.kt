package com.learnquest.mp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnquest.mp.data.model.LearningTopic
import com.learnquest.mp.model.Language
import com.learnquest.mp.ui.appStrings

/** Large card that lets the student resume their current topic. */
@Composable
fun ContinueLearningCard(
    topic: LearningTopic,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    appLanguage: Language = Language.ENGLISH,
    subjectLabel: String = topic.subject,
    topicLabel: String = topic.topic
) {
    val strings = appLanguage.appStrings()
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Simple illustration: emoji inside a colored circle (decorative, so hidden from TalkBack).
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clearAndSetSemantics { },
                    contentAlignment = Alignment.Center
                ) { Text(topic.emoji, fontSize = 36.sp) }

                Column(Modifier.weight(1f)) {
                    Text(subjectLabel, style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(topicLabel, style = MaterialTheme.typography.titleLarge)
                    Text(strings.level(topic.level), style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary)
                }
            }
            LinearProgressIndicator(
                progress = { topic.progressPercent / 100f },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Text(strings.complete(topic.progressPercent), style = MaterialTheme.typography.bodyMedium)
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
            ) { Text(strings.t("Continue Learning", "सीखना जारी रखें", "Continue Learning"), style = MaterialTheme.typography.titleMedium) }
        }
    }
}
