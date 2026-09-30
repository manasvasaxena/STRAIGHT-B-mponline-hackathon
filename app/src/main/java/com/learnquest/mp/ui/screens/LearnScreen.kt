package com.learnquest.mp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnquest.mp.model.MasteryState
import com.learnquest.mp.model.QuizQuestion
import com.learnquest.mp.ui.theme.ForestGreen
import com.learnquest.mp.ui.theme.SaffronPrimary

private data class LessonItem(
    val title: String,
    val subtitle: String,
    val markdown: String
)

private data class MiniGameItem(
    val title: String,
    val description: String,
    val question: String,
    val options: List<String>,
    val correctAnswer: Int
)

private val lessonItems = listOf(
    LessonItem(
        title = "Knowing Our Numbers",
        subtitle = "Class 6 Mathematics • 6 sections",
        markdown = """
            # Knowing Our Numbers

            ## Place Value

            A number is made up of digits. The place value of a digit depends on its position.

            Example: 5,43,216
            - 5 Lakhs (5,00,000)
            - 4 Ten Thousands (40,000)
            - 3 Thousands (3,000)
            - 2 Hundreds (200)
            - 1 Tens (10)
            - 6 Ones (6)

            ## Comparing Numbers

            Compare digits from the left. The first larger digit decides which number is greater.

            45,678 > 9,876
            56,432 > 54,321

            ## Practice

            Arrange these numbers in ascending order: 12, 25, 37, 48, 63.
        """.trimIndent()
    ),
    LessonItem(
        title = "Light & Reflection",
        subtitle = "Class 8 Science • Physics",
        markdown = """
            # Light & Reflection

            ## What is Reflection?

            When light strikes a smooth surface and returns to the same medium, the process is called reflection.

            ## Laws of Reflection

            - The angle of incidence is equal to the angle of reflection.
            - The incident ray, reflected ray and normal lie in the same plane.

            ## Remember

            A plane mirror forms an upright, virtual image of the same size as the object.
        """.trimIndent()
    ),
    LessonItem(
        title = "Photosynthesis Basics",
        subtitle = "Class 7 Science • Biology",
        markdown = """
            # Photosynthesis Basics

            Green plants prepare their food using sunlight, water and carbon dioxide. This process is called photosynthesis.

            ## Word Equation

            Carbon dioxide + Water --sunlight/chlorophyll--> Glucose + Oxygen

            ## Key Idea

            Chlorophyll captures sunlight. Oxygen is released into the air as a by-product.
        """.trimIndent()
    )
)

private val miniGameItems = listOf(
    MiniGameItem(
        title = "Rapid Fire Maths",
        description = "Solve one number question quickly.",
        question = "Which number is greater?",
        options = listOf("45,678", "9,876", "Both are equal"),
        correctAnswer = 0
    ),
    MiniGameItem(
        title = "Science Snap",
        description = "Choose the correct science fact.",
        question = "Which gas do plants use during photosynthesis?",
        options = listOf("Oxygen", "Carbon dioxide", "Nitrogen"),
        correctAnswer = 1
    ),
    MiniGameItem(
        title = "Memory Match",
        description = "Recall the key rule from your lessons.",
        question = "In reflection, the angle of incidence is…",
        options = listOf("Greater than reflection", "Less than reflection", "Equal to reflection"),
        correctAnswer = 2
    )
)

@Composable
fun LearnScreen(
    questions: List<QuizQuestion>,
    onQuizComplete: (scorePercentage: Int, masteryState: MasteryState) -> Unit,
    onMessage: (String) -> Unit = {}
) {
    var selectedLesson by remember { mutableStateOf<LessonItem?>(null) }
    var isQuizOpen by remember { mutableStateOf(false) }
    var selectedGame by remember { mutableStateOf<MiniGameItem?>(null) }
    var selectedGameAnswer by remember { mutableStateOf<Int?>(null) }

    when {
        selectedLesson != null -> {
            BackHandler { selectedLesson = null }
            MarkdownReaderScreen(
                title = selectedLesson!!.title,
                markdown = selectedLesson!!.markdown,
                isHindi = false,
                onBack = { selectedLesson = null }
            )
        }

        isQuizOpen -> {
            BackHandler { isQuizOpen = false }
            QuizScreen(
                questions = questions,
                onQuizComplete = onQuizComplete
            )
        }

        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Learn & Practice", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Pick a lesson, test your understanding, or play to revise.",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }

                item {
                    LearnSection(
                        title = "Lessons",
                        subtitle = "Read downloaded notes at your own pace.",
                        icon = Icons.Default.MenuBook,
                        iconTint = SaffronPrimary
                    ) {
                        lessonItems.forEach { lesson ->
                            LearnListCard(
                                icon = Icons.Default.Description,
                                title = lesson.title,
                                subtitle = lesson.subtitle,
                                actionLabel = "Read",
                                onClick = { selectedLesson = lesson }
                            )
                        }
                    }
                }

                item {
                    LearnSection(
                        title = "Quizzes",
                        subtitle = "Check mastery with adaptive questions.",
                        icon = Icons.Default.Quiz,
                        iconTint = ForestGreen
                    ) {
                        LearnListCard(
                            icon = Icons.Default.EmojiEvents,
                            title = "Adaptive Science Quiz",
                            subtitle = "${questions.size} questions • Mastery feedback",
                            actionLabel = "Start",
                            onClick = { isQuizOpen = true }
                        )
                    }
                }

                item {
                    LearnSection(
                        title = "Mini Games",
                        subtitle = "Short activities for fun revision.",
                        icon = Icons.Default.SportsEsports,
                        iconTint = Color(0xFF7C3AED)
                    ) {
                        miniGameItems.forEach { game ->
                            LearnListCard(
                                icon = Icons.Default.AutoStories,
                                title = game.title,
                                subtitle = game.description,
                                actionLabel = "Play",
                                onClick = {
                                    selectedGame = game
                                    selectedGameAnswer = null
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    selectedGame?.let { game ->
        AlertDialog(
            onDismissRequest = {
                selectedGame = null
                selectedGameAnswer = null
            },
            title = { Text(game.title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(game.question, fontWeight = FontWeight.SemiBold)
                    game.options.forEachIndexed { index, option ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedGameAnswer == index,
                                onClick = { selectedGameAnswer = index }
                            )
                            Text(option)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = selectedGameAnswer != null,
                    onClick = {
                        val correct = selectedGameAnswer == game.correctAnswer
                        onMessage(if (correct) "Correct! +10 XP" else "Good try — review the lesson and play again.")
                        selectedGame = null
                        selectedGameAnswer = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Text("Check answer")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    selectedGame = null
                    selectedGameAnswer = null
                }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun LearnSection(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}

@Composable
private fun LearnListCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = SaffronPrimary, modifier = Modifier.size(26.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text(subtitle, color = Color.Gray, fontSize = 12.sp)
            }
            OutlinedButton(onClick = onClick, contentPadding = PaddingValues(horizontal = 12.dp)) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(actionLabel)
            }
        }
    }
}
