package com.learnquest.mp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
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
import com.learnquest.mp.model.Language
import com.learnquest.mp.ui.appStrings

/**
 * Minimalist top header containing live connectivity and sync status.
 */
@Composable
fun TopStatusHeader(
    isOffline: Boolean,
    activeProfileName: String,
    xp: Int,
    streakDays: Int,
    isProtected: Boolean,
    pendingSyncCount: Int,
    onSyncClick: () -> Unit = {},
    appLanguage: Language = Language.ENGLISH
) {
    val strings = appLanguage.appStrings()
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

            // Right: live network status and sync queue indicator
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
                            contentDescription = strings.t("Sync Queue", "सिंक कतार", "Sync Queue"),
                            tint = if (pendingSyncCount > 0) SaffronPrimary else Color.Gray,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // This is read-only: connectivity comes from Android's validated network state.
                Box(
                    modifier = Modifier.size(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isOffline) Icons.Default.WifiOff else Icons.Default.Wifi,
                        contentDescription = if (isOffline) strings.t("Internet unavailable", "इंटरनेट उपलब्ध नहीं", "Internet unavailable") else strings.t("Internet connected", "इंटरनेट जुड़ा है", "Internet connected"),
                        tint = if (isOffline) Color(0xFFEF4444) else ForestGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
