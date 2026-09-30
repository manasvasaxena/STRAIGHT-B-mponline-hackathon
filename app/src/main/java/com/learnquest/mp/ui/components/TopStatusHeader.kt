package com.learnquest.mp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.AirplanemodeInactive
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnquest.mp.ui.theme.ForestGreen
import com.learnquest.mp.ui.theme.SaffronPrimary

/**
 * Minimalist Top Header containing clean quick action icons (Offline/Airplane toggle & Sync Status).
 */
@Composable
fun TopStatusHeader(
    isAirplaneMode: Boolean,
    onToggleAirplaneMode: () -> Unit,
    activeProfileName: String,
    xp: Int,
    streakDays: Int,
    isProtected: Boolean,
    pendingSyncCount: Int,
    onSyncClick: () -> Unit = {}
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Clean XP & Streak Badges
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = Color(0xFFFFF7ED),
                    shape = CircleShape
                ) {
                    Text(
                        text = "⚡ $xp",
                        color = SaffronPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    color = Color(0xFFEFF6FF),
                    shape = CircleShape
                ) {
                    Text(
                        text = "🔥 $streakDays",
                        color = Color(0xFF2563EB),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Right: Subtle Icon Controls (Airplane Mode & Sync Queue Indicator)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Sync Queue Badge/Icon
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onSyncClick() }
                        .padding(4.dp)
                ) {
                    BadgedBox(
                        badge = {
                            if (pendingSyncCount > 0) {
                                Badge(containerColor = SaffronPrimary) {
                                    Text("$pendingSyncCount", color = Color.White)
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync Queue",
                            tint = if (pendingSyncCount > 0) SaffronPrimary else Color.Gray,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Airplane Mode Minimal Toggle Button
                IconButton(
                    onClick = onToggleAirplaneMode,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isAirplaneMode) Icons.Default.AirplanemodeActive else Icons.Default.AirplanemodeInactive,
                        contentDescription = "Toggle Airplane Mode",
                        tint = if (isAirplaneMode) Color(0xFFEF4444) else ForestGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
