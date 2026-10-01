package com.learnquest.mp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnquest.mp.model.Language
import com.learnquest.mp.model.StudentProfile
import com.learnquest.mp.ui.appStrings
import com.learnquest.mp.ui.theme.SaffronPrimary

@Composable
fun ProfileScreen(
    profiles: List<StudentProfile> = sampleProfiles,
    activeProfileId: String = "1",
    selectedAppLanguage: Language = Language.HINDI,
    isOffline: Boolean = true,
    onSelectProfile: (String) -> Unit = {},
    onSelectLanguage: (Language) -> Unit = {},
    onCreateProfile: (String, String, Language) -> Unit = { _, _, _ -> }
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var currentActiveId by remember { mutableStateOf(activeProfileId) }
    var newName by remember { mutableStateOf("") }
    var newGrade by remember { mutableStateOf("Class 8") }
    var selectedLanguage by remember { mutableStateOf(selectedAppLanguage) }

    val activeProfile = profiles.find { it.id == currentActiveId } ?: profiles.firstOrNull()
    val isHindi = selectedAppLanguage == Language.HINDI
    val strings = selectedAppLanguage.appStrings()
    fun localized(hindi: String, english: String): String = strings.t(english, hindi, "$english / $hindi")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            activeProfile?.let { profile ->
                ActiveUserProfileCard(profile = profile, isHindi = isHindi, language = selectedAppLanguage)
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = localized("छात्र प्रोफ़ाइल (प्रोफ़ाइल बदलें)", "Student Profiles (Switch User)"),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = localized("एक ही डिवाइस पर बहु-उपयोगकर्ता चयन", "Shared device multi-user selection"),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(localized("नया जोड़ें", "Add New"))
                }
            }
        }

        items(profiles) { profile ->
            ProfileCard(
                profile = profile,
                isSelected = profile.id == currentActiveId,
                isHindi = isHindi,
                language = selectedAppLanguage,
                onSelect = {
                    currentActiveId = profile.id
                    onSelectProfile(profile.id)
                }
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = localized("⚙️ ऐप सेटिंग्स एवं भाषा", "⚙️ App Settings & Language"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = localized("🌐 पसंदीदा ऐप भाषा", "🌐 Preferred App Language"),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Language.entries.forEach { lang ->
                            val isSelected = selectedAppLanguage == lang
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedLanguage = lang
                                    onSelectLanguage(lang)
                                },
                                label = { Text(lang.displayName, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SaffronPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isOffline) Icons.Default.WifiOff else Icons.Default.Wifi,
                            contentDescription = null,
                            tint = if (isOffline) Color(0xFFEF4444) else Color(0xFF10B981)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                localized("इंटरनेट स्टेटस", "Internet Connectivity"),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                if (isOffline) {
                                    localized("ऑफ़लाइन मोड (स्थानीय डेटा का उपयोग)", "Offline Mode (Local Storage)")
                                } else {
                                    localized("ऑनलाइन (इंटरनेट से जुड़ा हुआ)", "Online (Connected)")
                                },
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))

                    SettingItem(
                        icon = Icons.Default.SdCard,
                        title = localized("ऑफ़लाइन स्टोरेज स्पेस", "Offline Storage Used"),
                        value = "14.2 MB / 500 MB"
                    )
                    SettingItem(
                        icon = Icons.Default.Sync,
                        title = localized("सिंक स्थिति", "Sync Status"),
                        value = localized("सभी परिवर्तन सिंक हैं", "All changes synced")
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(localized("नया छात्र जोड़ें", "Add Student Profile")) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text(localized("छात्र का नाम", "Student Name")) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newGrade,
                        onValueChange = { newGrade = it },
                        label = { Text(localized("कक्षा", "Class / Grade")) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            onCreateProfile(newName, newGrade, selectedLanguage)
                            newName = ""
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Text(localized("सहेजें", "Save"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(localized("रद्द करें", "Cancel"))
                }
            }
        )
    }
}

@Composable
fun ActiveUserProfileCard(profile: StudentProfile, isHindi: Boolean, language: Language = if (isHindi) Language.HINDI else Language.ENGLISH) {
    val strings = language.appStrings()
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFED7AA))
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(SaffronPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = profile.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7C2D12)
                    )
                    Text(
                        text = "${profile.grade} • ${profile.preferredLanguage.displayName}",
                        fontSize = 14.sp,
                        color = Color(0xFF9A3412)
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ProfileStat(icon = "⚡", label = "XP", value = "${profile.totalXp}")
                ProfileStat(icon = "🔥", label = strings.t("Streak", "स्ट्रीक", "Streak"), value = "${profile.streakDays} ${strings.t("Days", "दिन", "Days")}")
                ProfileStat(
                    icon = "🛡️",
                    label = strings.t("Shield", "सुरक्षा कवच", "Shield"),
                    value = if (profile.isStreakProtected) strings.t("Active", "सक्रिय", "Active") else strings.t("Off", "बंद", "Off")
                )
            }
        }
    }
}

@Composable
fun ProfileStat(icon: String, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "$icon $value", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF431407))
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun ProfileCard(
    profile: StudentProfile,
    isSelected: Boolean,
    isHindi: Boolean,
    onSelect: () -> Unit,
    language: Language = if (isHindi) Language.HINDI else Language.ENGLISH
) {
    val strings = language.appStrings()
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFFEF3C7) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = profile.name + if (isSelected) " (${strings.t("Active", "सक्रिय", "Active")})" else "",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.Black
                )
                Text(
                    text = "${profile.grade} | ${profile.preferredLanguage.displayName}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(selectedColor = SaffronPrimary)
            )
        }
    }
}

@Composable
fun SettingItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = title, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
    }
}

private val sampleProfiles = listOf(
    StudentProfile(
        id = "1",
        name = "Aarav Sharma",
        grade = "Class 8",
        preferredLanguage = Language.HINDI,
        totalXp = 1250,
        streakDays = 5,
        isStreakProtected = true
    ),
    StudentProfile(
        id = "2",
        name = "Priya Verma",
        grade = "Class 10",
        preferredLanguage = Language.HINDI,
        totalXp = 2100,
        streakDays = 12,
        isStreakProtected = true
    )
)
