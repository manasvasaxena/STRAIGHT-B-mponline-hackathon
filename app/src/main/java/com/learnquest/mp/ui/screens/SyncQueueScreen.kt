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
    onTriggerSync: () -> Unit
) {
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
                    text = "डेल्टा सिंक कतार / Sync Queue",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Delta updates wait locally until network reconnects",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Button(
                onClick = onTriggerSync,
                enabled = !isOffline && syncQueue.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
            ) {
                Text("Sync Now")
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
                    "⚠️ Connectivity is OFF. All local actions (XP, Quiz score, Lessons) are safe in SQLite / Room sync queue."
                else
                    "✅ Network Available. Ready to push delta updates to cloud backend with additive merge rules.",
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
                Text("No pending delta updates! All data synchronized.", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(syncQueue) { item ->
                    SyncQueueItemCard(item = item)
                }
            }
        }
    }
}

@Composable
fun SyncQueueItemCard(item: SyncQueueItem) {
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
                    text = "Action: ${item.actionType}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Payload: ${item.payload}",
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
                    text = item.status.name,
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
