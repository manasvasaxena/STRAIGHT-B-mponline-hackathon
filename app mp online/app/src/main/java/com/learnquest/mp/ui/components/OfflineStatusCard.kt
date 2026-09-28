package com.learnquest.mp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Shows connectivity + sync status. Uses text AND emoji, never color alone. */
@Composable
fun OfflineStatusCard(isOffline: Boolean, modifier: Modifier = Modifier) {
    val container = if (isOffline) MaterialTheme.colorScheme.tertiaryContainer
                    else MaterialTheme.colorScheme.secondaryContainer
    val content = if (isOffline) MaterialTheme.colorScheme.onTertiaryContainer
                  else MaterialTheme.colorScheme.onSecondaryContainer

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = container, contentColor = content)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                if (isOffline) "📱 Offline Mode" else "☁ Online",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                if (isOffline) "🟠 Offline" else "🟢 Synced",
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                if (isOffline) "Your downloaded lessons are available offline."
                else "Everything is up to date.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
