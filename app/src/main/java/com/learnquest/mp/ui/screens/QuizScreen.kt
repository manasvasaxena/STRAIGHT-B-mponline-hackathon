package com.learnquest.mp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnquest.mp.model.MasteryState
import com.learnquest.mp.model.Language
import com.learnquest.mp.model.QuizQuestion
import com.learnquest.mp.ui.appStrings
import com.learnquest.mp.ui.theme.ForestGreen
import com.learnquest.mp.ui.theme.SaffronPrimary
import com.learnquest.mp.ui.theme.WeakRed

/**
 * Quiz & Adaptive Learning Screen implementing the Rule-Based Mastery Engine (Key Innovation #7).
 */
@Composable
fun QuizScreen(
    questions: List<QuizQuestion>,
    onQuizComplete: (scorePercentage: Int, masteryState: MasteryState) -> Unit,
    appLanguage: Language = Language.ENGLISH
) {
    val strings = appLanguage.appStrings()
    var currentIndex by remember { mutableStateOf(0) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var correctAnswersCount by remember { mutableStateOf(0) }
    var isQuizSubmitted by remember { mutableStateOf(false) }
    var resultMastery by remember { mutableStateOf<MasteryState?>(null) }

    val currentQuestion = questions.getOrNull(currentIndex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = strings.t("Adaptive Quiz", "अनुकूली क्विज़", "Adaptive Quiz"),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (!isQuizSubmitted && currentQuestion != null) {
            // Question Progress
            Text(
                text = strings.t("Question ${currentIndex + 1} / ${questions.size}", "प्रश्न ${currentIndex + 1} / ${questions.size}", "Question ${currentIndex + 1} / ${questions.size}"),
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6))
            ) {
                Text(
                    text = currentQuestion.questionText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Options List
            currentQuestion.options.forEachIndexed { index, optionText ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .selectable(
                            selected = (selectedOption == index),
                            onClick = { selectedOption = index }
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (selectedOption == index),
                        onClick = { selectedOption = index }
                    )
                    Text(
                        text = optionText,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (selectedOption == currentQuestion.correctAnswerIndex) {
                        correctAnswersCount++
                    }
                    selectedOption = null

                    if (currentIndex < questions.size - 1) {
                        currentIndex++
                    } else {
                        // Calculate score & determine mastery
                        val percentage = (correctAnswersCount.toDouble() / questions.size * 100).toInt()
                        val state = when {
                            percentage >= 85 -> MasteryState.MASTERED
                            percentage >= 60 -> MasteryState.DEVELOPING
                            else -> MasteryState.WEAK
                        }
                        resultMastery = state
                        isQuizSubmitted = true
                        onQuizComplete(percentage, state)
                    }
                },
                enabled = selectedOption != null,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
            ) {
                Text(if (currentIndex < questions.size - 1) strings.t("Next question", "अगला प्रश्न", "Next question") else strings.t("Submit", "सबमिट करें", "Submit करें"))
            }

        } else if (isQuizSubmitted) {
            // Result & Adaptive Action Summary
            val percentage = (correctAnswersCount.toDouble() / questions.size * 100).toInt()
            val masteryText = resultMastery?.let { strings.mastery(it) }.orEmpty()
            val masteryAction = resultMastery?.let { strings.masteryAction(it) }.orEmpty()

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = strings.t("Quiz Score", "क्विज़ स्कोर", "Quiz Score"),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$percentage%",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = when (resultMastery) {
                            MasteryState.MASTERED -> ForestGreen
                            MasteryState.DEVELOPING -> SaffronPrimary
                            else -> WeakRed
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = strings.t("Mastery: $masteryText", "पकड़: $masteryText", "Mastery: $masteryText"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = strings.t("Action: $masteryAction", "कार्य: $masteryAction", "Action: $masteryAction"),
                        color = Color.DarkGray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            currentIndex = 0
                            correctAnswersCount = 0
                            isQuizSubmitted = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                    ) {
                        Text(strings.t("Repeat Practice", "पुनः प्रयास करें", "Repeat Practice"))
                    }
                }
            }
        }
    }
}
