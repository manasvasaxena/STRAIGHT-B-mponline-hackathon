package com.learnquest.mp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnquest.mp.data.model.QuickAction

/** Tappable card used in the 2x2 Quick Actions grid. */
@Composable
fun QuickActionCard(action: QuickAction, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().heightIn(min = 104.dp).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(action.emoji, fontSize = 32.sp, modifier = Modifier.clearAndSetSemantics { })
            Spacer(Modifier.height(6.dp))
            Text(action.label, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        }
    }
}
