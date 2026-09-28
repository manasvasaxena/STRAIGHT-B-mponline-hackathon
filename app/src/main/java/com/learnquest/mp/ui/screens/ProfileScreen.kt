package com.learnquest.mp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnquest.mp.model.Language
import com.learnquest.mp.model.StudentProfile
import com.learnquest.mp.ui.theme.SaffronPrimary

/**
 * Multi-Profile Selection screen for shared low-end devices in MP households (Key Innovation #3).
 */
@Composable
fun ProfileScreen(
    profiles: List<StudentProfile>,
    activeProfileId: String,
    onSelectProfile: (String) -> Unit,
    onCreateProfile: (String, String, Language) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newGrade by remember { mutableStateOf("Class 8") }
    var selectedLanguage by remember { mutableStateOf(Language.HINDI) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "छात्र प्रोफ़ाइल (Shared Device Profiles)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
            ) {
                Text("+ नया जोड़ें")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2FE)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Text(
                text = "💡 Multiple students can share this phone. Each student maintains separate XP, streaks, and learning history.",
                modifier = Modifier.padding(10.dp),
                fontSize = 12.sp,
                color = Color(0xFF0369A1)
            )
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(profiles) { profile ->
                ProfileCard(
                    profile = profile,
                    isSelected = profile.id == activeProfileId,
                    onSelect = { onSelectProfile(profile.id) }
                )
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp)
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
                    text = profile.name + if (isSelected) " (सक्रिय / Active)" else "",
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
