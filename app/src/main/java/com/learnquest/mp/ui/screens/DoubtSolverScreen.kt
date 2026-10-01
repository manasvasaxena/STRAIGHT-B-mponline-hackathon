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
import com.learnquest.mp.model.DoubtQuery
import com.learnquest.mp.model.Language
import com.learnquest.mp.ui.appStrings
import com.learnquest.mp.ui.theme.ForestGreen
import com.learnquest.mp.ui.theme.SaffronPrimary
import com.learnquest.mp.ui.theme.WarningOrange

/**
 * Doubt Solver Screen showcasing Offline FAQ Fallback (Key Innovation #5) & Teacher Escalation (Section 226-235).
 */
@Composable
fun DoubtSolverScreen(
    isOffline: Boolean,
    doubtsList: List<DoubtQuery>,
    onAskDoubt: (query: String) -> Unit,
    onEscalateToTeacher: (doubtId: String) -> Unit,
    appLanguage: Language = Language.ENGLISH
) {
    val strings = appLanguage.appStrings()
    var queryInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Text(
            text = strings.t("AI Doubt Solver", "शंका समाधान", "AI Doubt Solver"),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Mode explanation banner
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isOffline) Color(0xFFFEF3C7) else Color(0xFFE0F2FE)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (isOffline)
                    strings.t("⚡ OFFLINE MODE: Answers are served from cached curriculum FAQs & offline knowledge base.", "⚡ ऑफलाइन मोड: उत्तर कैश किए गए पाठ्यक्रम FAQ और ऑफलाइन ज्ञान आधार से दिए जाते हैं।", "⚡ OFFLINE MODE: Answers cached FAQs aur offline knowledge base se milenge.")
                else
                    strings.t("🌐 ONLINE MODE: Connected to Curriculum RAG Engine with LLM synthesis.", "🌐 ऑनलाइन मोड: पाठ्यक्रम RAG इंजन से जुड़ा है।", "🌐 ONLINE MODE: Curriculum RAG Engine se connected hai."),
                modifier = Modifier.padding(10.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Question Input Box
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = queryInput,
                onValueChange = { queryInput = it },
                placeholder = { Text(strings.t("Ask a question...", "प्रश्न पूछें...", "Question पूछें...")) },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (queryInput.isNotBlank()) {
                        onAskDoubt(queryInput)
                        queryInput = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
            ) {
                Text(strings.t("Ask", "पूछें", "Ask करें"))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = strings.t("Recent Queries", "हालिया प्रश्न", "Recent Queries"),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(doubtsList) { doubt ->
                DoubtItemCard(
                    doubt = doubt,
                    onEscalate = { onEscalateToTeacher(doubt.id) },
                    appLanguage = appLanguage
                )
            }
        }
    }
}

@Composable
fun DoubtItemCard(
    doubt: DoubtQuery,
    onEscalate: () -> Unit,
    appLanguage: Language = Language.ENGLISH
) {
    val strings = appLanguage.appStrings()
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "${strings.t("Q", "प्रश्न", "Q")}: ${doubt.questionText}",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                color = Color(0xFFF3F4F6),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = doubt.offlineAnswer ?: strings.t("No answer available yet.", "अभी उत्तर उपलब्ध नहीं है।", "Abhi answer available nahi hai."),
                    modifier = Modifier.padding(8.dp),
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (doubt.isResolvedOffline) strings.t("✓ Offline Match", "✓ ऑफलाइन मिलान", "✓ Offline Match") else strings.t("⚠️ Requires Online RAG / Escalation", "⚠️ ऑनलाइन RAG / शिक्षक को भेजना ज़रूरी", "⚠️ Online RAG / escalation चाहिए"),
                    fontSize = 11.sp,
                    color = if (doubt.isResolvedOffline) ForestGreen else WarningOrange,
                    fontWeight = FontWeight.Bold
                )

                if (doubt.isEscalatedToTeacher) {
                    Text(
                        text = strings.t("📩 Teacher Escalated", "📩 शिक्षक को भेजा गया", "📩 Teacher को भेजा गया"),
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    OutlinedButton(
                        onClick = onEscalate,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(strings.t("Escalate to teacher", "शिक्षक को भेजें", "Teacher को भेजें"), fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
