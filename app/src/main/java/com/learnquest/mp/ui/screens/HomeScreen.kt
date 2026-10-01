package com.learnquest.mp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnquest.mp.data.model.HomeData
import com.learnquest.mp.model.Language
import com.learnquest.mp.ui.components.*
import com.learnquest.mp.ui.appStrings
import com.learnquest.mp.ui.theme.SaffronPrimary
import java.util.Calendar

/** Top bar: app name, greeting, notification + profile icons. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    studentName: String,
    onNotifications: () -> Unit,
    onProfile: () -> Unit,
    appLanguage: Language = Language.ENGLISH
) {
    val strings = appLanguage.appStrings()
    TopAppBar(
        title = {
            Column {
                Text(
                    "SUTRA",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SaffronPrimary
                )
                Text(
                    "${greeting(appLanguage)}, $studentName!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
            }
        },
        actions = {
            IconButton(onClick = onNotifications) {
                Icon(Icons.Filled.Notifications, contentDescription = strings.t("Notifications", "सूचनाएं", "Notifications"), tint = Color.DarkGray)
            }
            IconButton(onClick = onProfile) {
                Surface(
                    shape = CircleShape,
                    color = SaffronPrimary.copy(alpha = 0.2f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Person, contentDescription = strings.t("Open profile", "प्रोफ़ाइल खोलें", "Profile खोलें"), tint = SaffronPrimary)
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
    )
}

/** Main home dashboard. Connects every feature directly. */
@Composable
fun HomeScreen(
    data: HomeData,
    isOffline: Boolean,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    appLanguage: Language = Language.ENGLISH
) {
    val strings = appLanguage.appStrings()
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.widthIn(max = 640.dp).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item(key = "progress") { ProgressCard(data.progress, appLanguage = appLanguage) }

            // Continue Learning
            item(key = "continue") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionTitle(strings.t("Continue Learning", "सीखना जारी रखें", "Continue Learning"))
                    ContinueLearningCard(
                        topic = data.continueTopic,
                        onContinue = { onNavigate("learn") },
                        appLanguage = appLanguage,
                        subjectLabel = localizedHomeTopic(data.continueTopic.subject, strings),
                        topicLabel = localizedHomeTopic(data.continueTopic.topic, strings)
                    )
                }
            }

            // Quick Navigation Hub
            item(key = "quick_hub") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionTitle(strings.t("Quick Actions Hub", "त्वरित कार्य", "Quick Actions Hub"))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        HomeQuickActionTile(
                            title = strings.t("Ask AI Doubt", "AI से सवाल पूछें", "AI Doubt पूछें"),
                            subtitle = strings.t("Instant Answers", "तुरंत उत्तर", "Instant Answers"),
                            icon = Icons.Default.Psychology,
                            badgeColor = Color(0xFF3B82F6),
                            onClick = { onNavigate("doubt_solver") },
                            modifier = Modifier.weight(1f)
                        )
                        HomeQuickActionTile(
                            title = strings.t("Quiz Battle", "क्विज़ बैटल", "Quiz Battle"),
                            subtitle = strings.t("P2P Offline", "पी2पी ऑफ-लाइन", "P2P Offline"),
                            icon = Icons.Default.EmojiEvents,
                            badgeColor = SaffronPrimary,
                            onClick = { onNavigate("quiz_battle") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        HomeQuickActionTile(
                            title = strings.t("Explore World", "दुनिया खोजें", "Explore World"),
                            subtitle = strings.t("Hindi, Eng, Sans", "हिन्दी, अंग्रेज़ी, संस्कृत", "Hindi, Eng, Sans"),
                            icon = Icons.Default.Explore,
                            badgeColor = Color(0xFFF59E0B),
                            onClick = { onNavigate("explore") },
                            modifier = Modifier.weight(1f)
                        )
                        HomeQuickActionTile(
                            title = strings.t("Sync Queue", "सिंक कतार", "Sync Queue"),
                            subtitle = strings.t("Pending Items", "लंबित आइटम", "Pending Items"),
                            icon = Icons.Default.Sync,
                            badgeColor = Color(0xFF8B5CF6),
                            onClick = { onNavigate("sync_queue") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Explore Learning World Zones
            item(key = "explore_zones") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionTitle(strings.t("Learning World", "सीखने की दुनिया", "Learning World"))
                        TextButton(onClick = { onNavigate("explore") }) {
                            Text(strings.t("See All", "सभी देखें", "See All"), color = SaffronPrimary, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp), tint = SaffronPrimary)
                        }
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(data.zones, key = { it.id }) { zone ->
                            LearningZoneCard(zone, onClick = {
                                if (zone.id == "career") {
                                    onNavigate("progress")
                                } else {
                                    onNavigate("explore")
                                }
                            }, appLanguage = appLanguage, nameLabel = localizedHomeZone(zone.name, strings))
                        }
                    }
                }
            }

            // Daily Quiz Challenge
            item(key = "challenge") {
                DailyChallengeCard(
                    data.dailyChallenge,
                    onStart = { onNavigate("learn") },
                    appLanguage = appLanguage
                )
            }

            // Scholarships & Opportunities Banner
            item(key = "opportunities") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate("progress") },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF10B981),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.School, contentDescription = null, tint = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                strings.t("MP Scholarships & Pathways", "MP छात्रवृत्ति और अवसर", "MP Scholarships & Pathways"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF065F46)
                            )
                            Text(
                                strings.t("Explore state schemes, super 100, and career guides", "राज्य योजनाएं, सुपर 100 और करियर गाइड देखें", "State schemes, Super 100 aur career guides देखें"),
                                fontSize = 12.sp,
                                color = Color(0xFF047857)
                            )
                        }
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color(0xFF047857))
                    }
                }
            }

            // P2P Quiz Battle Dedicated Bottom Banner Button
            item(key = "p2p_quiz_battle_banner") {
                Card(
                    onClick = { onNavigate("quiz_battle") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SaffronPrimary),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "⚔️ " + strings.t("P2P Quiz Battle", "पी2पी क्विज़ बैटल", "P2P Quiz Battle"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                strings.t("Challenge nearby friends offline without internet!", "बिना इंटरनेट के पास के दोस्तों को चुनौती दें!", "Nearby friends ko challenge karein without internet!"),
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White,
                            modifier = Modifier.padding(start = 6.dp)
                        ) {
                            Text(
                                text = strings.t("PLAY NOW", "खेलें", "PLAY NOW"),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = SaffronPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeQuickActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badgeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = badgeColor.copy(alpha = 0.15f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                Text(subtitle, fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
}

private fun greeting(language: Language): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> language.appStrings().t("Good morning", "सुप्रभात", "Good morning")
        hour < 17 -> language.appStrings().t("Good afternoon", "नमस्कार", "Good afternoon")
        else -> language.appStrings().t("Good evening", "शुभ संध्या", "Good evening")
    }
}

private fun localizedHomeTopic(value: String, strings: com.learnquest.mp.ui.AppStrings): String = when (value) {
    "Mathematics" -> strings.t(value, "गणित", "Maths")
    "Fractions" -> strings.t(value, "भिन्न", "Fractions")
    else -> value
}

private fun localizedHomeZone(value: String, strings: com.learnquest.mp.ui.AppStrings): String = when (value) {
    "Science Forest" -> strings.t(value, "विज्ञान वन", "Science Forest")
    "Mathematics Mountain" -> strings.t(value, "गणित पर्वत", "Maths Mountain")
    "Computer City" -> strings.t(value, "कंप्यूटर शहर", "Computer City")
    "Language Village" -> strings.t(value, "भाषा गांव", "Language Village")
    "Career Campus" -> strings.t(value, "करियर कैंपस", "Career Campus")
    else -> value
}
