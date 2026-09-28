package com.learnquest.mp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnquest.mp.ui.theme.*

/**
 * Top Status Bar displaying Network Mode, Streak Shield, XP, and Current Active Profile.
 */
@Composable
fun TopStatusHeader(
    isAirplaneMode: Boolean,
    onToggleAirplaneMode: (Boolean) -> Unit,
    activeProfileName: String,
    xp: Int,
    streakDays: Int,
    isProtected: Boolean,
    pendingSyncCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = if (isAirplaneMode) Color(0xFF2D3748) else SaffronPrimary),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Row 1: Profile & Network Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "छात्र / Student: $activeProfileName",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = if (isAirplaneMode) "OFFLINE MODE (ऑफलाइन)" else "ONLINE MODE (ऑनलाइन)",
                        color = if (isAirplaneMode) Color(0xFFFFC107) else Color(0xFFE2E8F0),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Airplane Mode",
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Switch(
                        checked = isAirplaneMode,
                        onCheckedChange = onToggleAirplaneMode,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Red,
                            checkedTrackColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: Gamification Badges (XP, Streak Shield, Delta Sync Status)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // XP Badge
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "⚡ $xp XP",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp
                    )
                }

                // Streak Shield Badge
                Surface(
                    color = if (isProtected) StreakShieldBlue else WarningOrange,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (isProtected) "🛡️ Streak Shield ($streakDays Days)" else "🔥 Streak: $streakDays Days",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp
                    )
                }

                // Sync Queue Indicator
                Surface(
                    color = if (pendingSyncCount > 0) WarningOrange else ForestGreen,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "🔄 Sync: $pendingSyncCount Queue",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
