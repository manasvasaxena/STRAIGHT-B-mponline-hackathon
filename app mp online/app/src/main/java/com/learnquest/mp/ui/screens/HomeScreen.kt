package com.learnquest.mp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.learnquest.mp.data.model.HomeData
import com.learnquest.mp.ui.components.*
import java.util.Calendar

/** Top bar: app name, greeting, notification + profile icons. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(studentName: String, onNotifications: () -> Unit, onProfile: () -> Unit) {
    TopAppBar(
        title = {
            Column {
                Text("LearnQuest MP", style = MaterialTheme.typography.titleLarge)
                Text("${greeting()}, $studentName!", style = MaterialTheme.typography.bodyMedium)
            }
        },
        actions = {
            IconButton(onClick = onNotifications) {
                Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
            }
            IconButton(onClick = onProfile) {
                Icon(Icons.Filled.Person, contentDescription = "Open profile")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
    )
}

/** Main home dashboard. Everything is one LazyColumn so it scrolls smoothly. */
@Composable
fun HomeScreen(
    data: HomeData,
    isOffline: Boolean,
    onMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Box + widthIn keeps the layout readable on tablets (content is centered, max 640dp wide).
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.widthIn(max = 640.dp).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item(key = "tagline") {
                Text(
                    "Learn Anywhere. Learn in Your Language. Level Up Your Future.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item(key = "offline") { OfflineStatusCard(isOffline) }
            item(key = "progress") { ProgressCard(data.progress) }

            item(key = "continue") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionTitle("Continue Learning")
                    ContinueLearningCard(
                        topic = data.continueTopic,
                        onContinue = { onMessage("Opening ${data.continueTopic.topic} (coming soon)") }
                    )
                }
            }

            item(key = "explore") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionTitle("Explore Learning World")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(data.zones, key = { it.id }) { zone ->
                            LearningZoneCard(zone, onClick = { onMessage("${zone.name}: coming soon") })
                        }
                    }
                }
            }

            item(key = "challenge") {
                DailyChallengeCard(data.dailyChallenge, onStart = { onMessage("Daily challenge: coming soon") })
            }

            item(key = "quick") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionTitle("Quick Actions")
                    // 2x2 grid built from Rows. (A LazyVerticalGrid inside a LazyColumn would crash.)
                    data.quickActions.chunked(2).forEach { rowActions ->
                        Row(
                            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowActions.forEach { action ->
                                QuickActionCard(
                                    action = action,
                                    onClick = { onMessage("${action.label}: Coming soon") },
                                    modifier = Modifier.weight(1f).fillMaxHeight()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleLarge)
}

private fun greeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }
}
