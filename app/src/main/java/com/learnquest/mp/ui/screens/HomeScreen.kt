package com.learnquest.mp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnquest.mp.model.LearningLevel
import com.learnquest.mp.model.MasteryState
import com.learnquest.mp.ui.theme.ForestGreen
import com.learnquest.mp.ui.theme.MasteredGreen
import com.learnquest.mp.ui.theme.SaffronPrimary
import com.learnquest.mp.ui.theme.WeakRed

/**
 * HomeScreen showcasing offline-first level downloads, low-bandwidth content packs, and level progression.
 */
@Composable
fun HomeScreen(
    levels: List<LearningLevel>,
    onDownloadLevel: (String) -> Unit,
    onStartLesson: (String) -> Unit,
    isOffline: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Text(
            text = "पाठ्यक्रम स्तर / Learning Levels",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (isOffline) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    text = "ℹ️ Offline Mode Active: You can study downloaded modules. New downloads require network.",
                    modifier = Modifier.padding(10.dp),
                    fontSize = 12.sp,
                    color = Color(0xFF92400E)
                )
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(levels) { level ->
                LearningLevelCard(
                    level = level,
                    onDownload = { onDownloadLevel(level.id) },
                    onStart = { onStartLesson(level.id) },
                    isOffline = isOffline
                )
            }
        }
    }
}

@Composable
fun LearningLevelCard(
    level: LearningLevel,
    onDownload: () -> Unit,
    onStart: () -> Unit,
    isOffline: Boolean
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Level ${level.levelNumber}: ${level.titleHindi}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${level.titleEnglish} (${level.subject})",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }

                // Mastery Status Badge
                val masteryColor = when (level.masteryState) {
                    MasteryState.MASTERED -> MasteredGreen
                    MasteryState.DEVELOPING -> SaffronPrimary
                    MasteryState.WEAK -> WeakRed
                }

                Surface(
                    color = masteryColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = level.masteryState.label,
                        color = masteryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Download & Progress Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lessons: ${level.downloadedLessons}/${level.totalLessons} | Size: ${level.downloadSizeMb} MB",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )

                if (level.isDownloaded) {
                    Button(
                        onClick = onStart,
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("अध्ययन करें (Start)")
                    }
                } else {
                    Button(
                        onClick = onDownload,
                        enabled = !isOffline,
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("डाउनलोड (${level.downloadSizeMb} MB)")
                    }
                }
            }
        }
    }
}
