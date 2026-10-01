package com.learnquest.mp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnquest.mp.model.Language
import com.learnquest.mp.ui.appStrings

/** Temporary screen for tabs that are not built yet. */
@Composable
fun PlaceholderScreen(
    title: String,
    emoji: String,
    modifier: Modifier = Modifier,
    appLanguage: Language = Language.ENGLISH
) {
    val strings = appLanguage.appStrings()
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(emoji, fontSize = 64.sp)
        Spacer(Modifier.height(12.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Text(strings.t("Coming soon", "जल्द उपलब्ध होगा", "Coming soon"), style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
