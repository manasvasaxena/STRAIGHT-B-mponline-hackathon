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
    var selectedLanguage by remember { mutableStateOf(Language.HINDI) }

    val activeProfile = profiles.find { it.id == currentActiveId } ?: profiles.firstOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            activeProfile?.let { profile ->
                ActiveUserProfileCard(profile = profile)
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
                        text = "छात्र प्रोफ़ाइल (Switch User)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Shared device multi-user selection",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("नया जोड़ें")
                }
            }
        }

        items(profiles) { profile ->
            ProfileCard(
                profile = profile,
                isSelected = profile.id == currentActiveId,
                onSelect = {
                    currentActiveId = profile.id
                    onSelectProfile(profile.id)
                }
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⚙️ ऐप सेटिंग्स & कनेक्टिविटी Control",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isOffline) Icons.Default.WifiOff else Icons.Default.Wifi,
                                contentDescription = if (isOffline) "Internet unavailable" else "Internet connected",
                                tint = if (isOffline) Color(0xFFEF4444) else Color(0xFF10B981)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("इंटरनेट कनेक्टिविटी / Internet", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(
                                    if (isOffline) "इंटरनेट उपलब्ध नहीं (Offline)" else "सत्यापित इंटरनेट कनेक्शन (Online)",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Text(
                        text = "🌐 ऐप भाषा / App Language",
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
                                onClick = { onSelectLanguage(lang) },
                                label = { Text(lang.displayName, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SaffronPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    SettingItem(
                        icon = Icons.Default.WifiOff,
                        title = "Offline Storage Used",
                        value = "14.2 MB / 500 MB"
                    )
                    SettingItem(
                        icon = Icons.Default.Sync,
                        title = "Sync Status",
                        value = "All changes synced"
                    )
                    SettingItem(
                        icon = Icons.Default.Language,
                        title = "Default Language",
                        value = "Hindi / हिन्दी"
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("नया छात्र जोड़ें (Add Student)") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("नाम (Student Name)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newGrade,
                        onValueChange = { newGrade = it },
                        label = { Text("कक्षा (Class/Grade)") },
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
                    }
                ) {
                    Text("सहेजे (Save)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("रद्द करें (Cancel)")
                }
            }
        )
    }
}

@Composable
fun ActiveUserProfileCard(profile: StudentProfile) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(SaffronPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = profile.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${profile.grade} • ${profile.preferredLanguage.displayName}",
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ProfileStat(icon = "⚡", label = "XP", value = "${profile.totalXp}")
                ProfileStat(icon = "🔥", label = "Streak", value = "${profile.streakDays} Days")
                ProfileStat(
                    icon = "🛡️",
                    label = "Shield",
                    value = if (profile.isStreakProtected) "Active" else "Off"
                )
            }
        }
    }
}

@Composable
fun ProfileStat(icon: String, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "$icon $value", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun ProfileCard(
    profile: StudentProfile,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFFEF3C7) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(12.dp)
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
                    text = profile.name + if (isSelected) " (सक्रिय)" else "",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "${profile.grade} | ${profile.preferredLanguage.displayName}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "⚡ XP: ${profile.totalXp} | 🔥 Streak: ${profile.streakDays} Days",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray
                )
            }

            RadioButton(
                selected = isSelected,
                onClick = onSelect
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
