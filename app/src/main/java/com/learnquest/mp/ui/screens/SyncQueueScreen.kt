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
import com.learnquest.mp.model.SyncQueueItem
import com.learnquest.mp.model.SyncStatus
import com.learnquest.mp.model.Language
import com.learnquest.mp.ui.appStrings
import com.learnquest.mp.ui.theme.ForestGreen
import com.learnquest.mp.ui.theme.SaffronPrimary
import com.learnquest.mp.ui.theme.WarningOrange

/**
 * Screen demonstrating Delta Synchronization & Offline Queueing (Section 180-198).
 */
@Composable
fun SyncQueueScreen(
    syncQueue: List<SyncQueueItem>,
    isOffline: Boolean,
    onTriggerSync: () -> Unit,
    appLanguage: Language = Language.ENGLISH
) {
    val strings = appLanguage.appStrings()
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
            Column {
                Text(
                    text = strings.t("Sync Queue", "सिंक कतार", "Sync Queue"),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = strings.t("Delta updates wait locally until network reconnects", "नेटवर्क जुड़ने तक बदलाव स्थानीय रूप से सुरक्षित रहते हैं", "Delta updates network reconnect hone tak locally wait karte hain"),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Button(
                onClick = onTriggerSync,
                enabled = !isOffline && syncQueue.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
            ) {
                Text(strings.t("Sync Now", "अभी सिंक करें", "Abhi Sync करें"))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isOffline) Color(0xFFFEF3C7) else Color(0xFFDCFCE7)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (isOffline)
                    strings.t("⚠️ Connectivity is OFF. All local actions (XP, Quiz score, Lessons) are safe in SQLite / Room sync queue.", "⚠️ कनेक्टिविटी बंद है। सभी स्थानीय कार्य (XP, क्विज़ स्कोर, पाठ) SQLite / Room सिंक कतार में सुरक्षित हैं।", "⚠️ Connectivity OFF hai. XP, quiz score aur lessons sync queue mein safe hain.")
                else
                    strings.t("✅ Network Available. Ready to push delta updates to cloud backend with additive merge rules.", "✅ नेटवर्क उपलब्ध है। डेल्टा अपडेट क्लाउड पर भेजने के लिए तैयार हैं।", "✅ Network available hai. Delta updates cloud par push ke liye ready hain."),
                modifier = Modifier.padding(10.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (syncQueue.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(30.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(strings.t("No pending delta updates! All data synchronized.", "कोई लंबित अपडेट नहीं! सभी डेटा सिंक है।", "No pending updates! All data synced hai."), color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(syncQueue) { item ->
                    SyncQueueItemCard(item = item, appLanguage = appLanguage)
                }
            }
        }
    }
}

@Composable
fun SyncQueueItemCard(item: SyncQueueItem, appLanguage: Language = Language.ENGLISH) {
    val strings = appLanguage.appStrings()
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${strings.t("Action", "कार्य", "Action")}: ${item.actionType}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "${strings.t("Payload", "डेटा", "Payload")}: ${item.payload}",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
            }

            Surface(
                color = when (item.status) {
                    SyncStatus.PENDING -> WarningOrange.copy(alpha = 0.2f)
                    SyncStatus.SYNCED -> ForestGreen.copy(alpha = 0.2f)
                    else -> Color.LightGray
                },
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = when (item.status) {
                        SyncStatus.PENDING -> strings.t("PENDING", "लंबित", "PENDING")
                        SyncStatus.SYNCING -> strings.t("SYNCING", "सिंक हो रहा", "SYNCING")
                        SyncStatus.SYNCED -> strings.t("SYNCED", "सिंक हो गया", "SYNCED")
                        SyncStatus.FAILED -> strings.t("FAILED", "असफल", "FAILED")
                    },
                    color = when (item.status) {
                        SyncStatus.PENDING -> WarningOrange
                        SyncStatus.SYNCED -> ForestGreen
                        else -> Color.DarkGray
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
