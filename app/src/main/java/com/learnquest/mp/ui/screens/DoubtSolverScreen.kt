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
    onEscalateToTeacher: (doubtId: String) -> Unit
) {
    var queryInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Text(
            text = "शंका समाधान / AI Doubt Solver",
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
                    "⚡ OFFLINE MODE: Answers are served from cached curriculum FAQs & offline knowledge base."
                else
                    "🌐 ONLINE MODE: Connected to Curriculum RAG Engine with LLM synthesis.",
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
                placeholder = { Text("प्रश्न पूछें... (Ask a doubt in Hindi/English)") },
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
                Text("पूछें")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "हालिया प्रश्न / Recent Queries",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(doubtsList) { doubt ->
                DoubtItemCard(
                    doubt = doubt,
                    onEscalate = { onEscalateToTeacher(doubt.id) }
                )
            }
        }
    }
}

@Composable
fun DoubtItemCard(
    doubt: DoubtQuery,
    onEscalate: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Q: ${doubt.questionText}",
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
                    text = doubt.offlineAnswer ?: "उत्तरात्मक उत्तर प्राप्त नहीं हुआ।",
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
                    text = if (doubt.isResolvedOffline) "✓ Offline Match (कैश्ड उत्तर)" else "⚠️ Requires Online RAG / Escalation",
                    fontSize = 11.sp,
                    color = if (doubt.isResolvedOffline) ForestGreen else WarningOrange,
                    fontWeight = FontWeight.Bold
                )

                if (doubt.isEscalatedToTeacher) {
                    Text(
                        text = "📩 Teacher Escalated",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    OutlinedButton(
                        onClick = onEscalate,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("शिक्षक को भेजें (Escalate)", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
