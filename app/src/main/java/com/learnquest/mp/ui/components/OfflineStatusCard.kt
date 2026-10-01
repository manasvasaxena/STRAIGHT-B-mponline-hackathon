package com.learnquest.mp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.learnquest.mp.model.Language
import com.learnquest.mp.ui.appStrings

/** Shows connectivity + sync status. Uses text AND emoji, never color alone. */
@Composable
fun OfflineStatusCard(
    isOffline: Boolean,
    modifier: Modifier = Modifier,
    appLanguage: Language = Language.ENGLISH
) {
    val strings = appLanguage.appStrings()
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
                if (isOffline) strings.t("📱 Offline Mode", "📱 ऑफलाइन मोड", "📱 Offline Mode") else strings.t("☁ Online", "☁ ऑनलाइन", "☁ Online"),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                if (isOffline) strings.t("🟠 Offline", "🟠 ऑफलाइन", "🟠 Offline") else strings.t("🟢 Synced", "🟢 सिंक हो गया", "🟢 Synced"),
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                if (isOffline) strings.t("Your downloaded lessons are available offline.", "आपके डाउनलोड किए हुए पाठ ऑफलाइन उपलब्ध हैं।", "Downloaded lessons offline available hain.")
                else strings.t("Internet connection verified. Pending changes can sync.", "इंटरनेट कनेक्शन सत्यापित है। लंबित बदलाव सिंक हो सकते हैं।", "Internet verified hai. Pending changes sync ho sakte hain."),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
