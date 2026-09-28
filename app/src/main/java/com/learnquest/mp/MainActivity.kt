package com.learnquest.mp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import com.learnquest.mp.model.*
import com.learnquest.mp.ui.components.TopStatusHeader
import com.learnquest.mp.ui.screens.*
import com.learnquest.mp.ui.theme.LearnQuestTheme
import com.learnquest.mp.ui.theme.SaffronPrimary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LearnQuestTheme {
                LearnQuestApp()
            }
        }
    }
}

enum class NavigationTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    QUIZ("Adaptive Quiz", Icons.Default.CheckCircle),
    DOUBTS("Doubt Solver", Icons.Default.QuestionAnswer),
    OPPORTUNITIES("Scholarships", Icons.Default.Star),
    PROFILES("Profiles", Icons.Default.Person),
    SYNC("Sync Queue", Icons.Default.Refresh)
}

@Composable
fun LearnQuestApp() {
    // Shared State & Offline Toggle
    var isAirplaneMode by remember { mutableStateOf(true) } // Default to Airplane Mode for demo
    var currentTab by remember { mutableStateOf(NavigationTab.HOME) }

    // Dummy Profiles for Multi-Profile Shared Device
    var profiles by remember {
        mutableStateOf(
            listOf(
                StudentProfile(id = "p1", name = "Aarav Sharma", grade = "Class 8", totalXp = 450, streakDays = 5, isStreakProtected = true),
                StudentProfile(id = "p2", name = "Priya Tribal", grade = "Class 7", totalXp = 280, streakDays = 3, isStreakProtected = true)
            )
        )
    }
    var activeProfileId by remember { mutableStateOf("p1") }
    val activeProfile = profiles.find { it.id == activeProfileId } ?: profiles.first()

    // Dummy Learning Levels
    var levels by remember {
        mutableStateOf(
            listOf(
                LearningLevel("l1", "विज्ञान: प्रकाश एवं परावर्तन", "Science: Light & Reflection", "Science", 1, 5, 5, 2.4, isDownloaded = true, masteryState = MasteryState.MASTERED),
                LearningLevel("l2", "गणित: बीजगणित मूल बातें", "Math: Algebra Fundamentals", "Mathematics", 2, 6, 0, 3.1, isDownloaded = false, masteryState = MasteryState.WEAK),
                LearningLevel("l3", "पर्यावरण अध्ययन", "Environmental Studies", "EVS", 3, 4, 4, 1.8, isDownloaded = true, masteryState = MasteryState.DEVELOPING)
            )
        )
    }

    // Dummy Quiz Questions
    val quizQuestions = listOf(
        QuizQuestion("q1", "प्रकाश की गति कितनी होती है? (What is the speed of light?)", listOf("3 x 10^8 m/s", "3 x 10^6 m/s", "3000 m/s", "300 km/s"), 0, "प्रकाश की गति निर्वात में 3 x 10^8 मीटर/सेकंड होती है।"),
        QuizQuestion("q2", "पौधों में प्रकाश संश्लेषण के लिए क्या आवश्यक है?", listOf("सूर्य का प्रकाश", "जल", "कार्बन डाइऑक्साइड", "उपरोक्त सभी"), 3, "पौधों को प्रकाश संश्लेषण के लिए सूर्य का प्रकाश, जल और CO2 सभी चाहिए।")
    )

    // Dummy Doubts List (Offline Fallback & Teacher Escalation)
    var doubtsList by remember {
        mutableStateOf(
            listOf(
                DoubtQuery(id = "d1", studentId = activeProfileId, questionText = "प्रकाश का परावर्तन क्या है?", isResolvedOffline = true, offlineAnswer = "जब प्रकाश किसी चिकनी सतह से टकराकर वापस लौटता है तो इसे परावर्तन कहते हैं। (Cached FAQ)"),
                DoubtQuery(id = "d2", studentId = activeProfileId, questionText = "बीजगणित में 'x' का मान कैसे निकालते हैं?", isResolvedOffline = false, offlineAnswer = "ऑनलाइन RAG या शिक्षक से उत्तर की प्रतीक्षा है।", isEscalatedToTeacher = true)
            )
        )
    }

    // Dummy Delta Sync Queue
    var syncQueue by remember {
        mutableStateOf(
            listOf(
                SyncQueueItem("s1", "LESSON_COMPLETE", payload = "{levelId: 'l1', lesson: 5}", status = SyncStatus.PENDING),
                SyncQueueItem("s2", "XP_ADD", payload = "{xp: 50, streakProtected: true}", status = SyncStatus.PENDING)
            )
        )
    }

    // Dummy Scholarships & Career Pathways
    val scholarships = listOf(
        Scholarship("sc1", "MP Tribal Welfare Post-Matric Scholarship", "Government of MP", "Tribal / ST", "Class 9-12 students in MP", "31 Oct 2026", "₹8,000 / year", true, "2026-09-15"),
        Scholarship("sc2", "Rural Science Talent Search", "MP Education Dept", "Merit Cum Means", "Score ≥ 75% in Class 8", "15 Nov 2026", "₹12,000 / year", true, "2026-09-20")
    )

    val careers = listOf(
        CareerPathway("c1", "Agriculture Extension Officer", "Government / Agri-tech", "B.Sc Agriculture", "Help farmers adopt modern sustainable agricultural practices.", listOf("Science", "Biology", "Environment")),
        CareerPathway("c2", "Solar Energy Technician", "Renewable Energy", "ITI / Diploma", "Install and maintain solar panels in rural off-grid setups.", listOf("Physics", "Mathematics"))
    )

    Scaffold(
        topBar = {
            TopStatusHeader(
                isAirplaneMode = isAirplaneMode,
                onToggleAirplaneMode = { isAirplaneMode = it },
                activeProfileName = activeProfile.name,
                xp = activeProfile.totalXp,
                streakDays = activeProfile.streakDays,
                isProtected = activeProfile.isStreakProtected,
                pendingSyncCount = syncQueue.count { it.status == SyncStatus.PENDING }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SaffronPrimary,
                            selectedTextColor = SaffronPrimary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (currentTab) {
                NavigationTab.HOME -> HomeScreen(
                    levels = levels,
                    onDownloadLevel = { levelId ->
                        levels = levels.map { if (it.id == levelId) it.copy(isDownloaded = true) else it }
                    },
                    onStartLesson = { /* Navigate to lesson viewer */ },
                    isOffline = isAirplaneMode
                )

                NavigationTab.QUIZ -> QuizScreen(
                    questions = quizQuestions,
                    onQuizComplete = { score, mastery ->
                        // Award XP & add to sync queue
                        val xpEarned = score * 2
                        profiles = profiles.map {
                            if (it.id == activeProfileId) it.copy(totalXp = it.totalXp + xpEarned) else it
                        }
                        syncQueue = syncQueue + SyncQueueItem(
                            actionType = "QUIZ_RESULT",
                            payload = "{score: $score, mastery: ${mastery.name}}"
                        )
                    }
                )

                NavigationTab.DOUBTS -> DoubtSolverScreen(
                    isOffline = isAirplaneMode,
                    doubtsList = doubtsList,
                    onAskDoubt = { query ->
                        val newDoubt = DoubtQuery(
                            studentId = activeProfileId,
                            questionText = query,
                            isResolvedOffline = isAirplaneMode,
                            offlineAnswer = if (isAirplaneMode) "कैश्ड उत्तर: प्रकाश सीधी रेखा में गमन करता है।" else "RAG AI Result: Answer generated with online knowledge base."
                        )
                        doubtsList = listOf(newDoubt) + doubtsList
                    },
                    onEscalateToTeacher = { doubtId ->
                        doubtsList = doubtsList.map {
                            if (it.id == doubtId) it.copy(isEscalatedToTeacher = true) else it
                        }
                        syncQueue = syncQueue + SyncQueueItem(
                            actionType = "DOUBT_ESCALATE",
                            payload = "{doubtId: '$doubtId'}"
                        )
                    }
                )

                NavigationTab.OPPORTUNITIES -> OpportunitiesScreen(
                    scholarships = scholarships,
                    careers = careers
                )

                NavigationTab.PROFILES -> ProfileScreen(
                    profiles = profiles,
                    activeProfileId = activeProfileId,
                    onSelectProfile = { activeProfileId = it },
                    onCreateProfile = { name, grade, lang ->
                        val newP = StudentProfile(name = name, grade = grade, preferredLanguage = lang)
                        profiles = profiles + newP
                        activeProfileId = newP.id
                    }
                )

                NavigationTab.SYNC -> SyncQueueScreen(
                    syncQueue = syncQueue,
                    isOffline = isAirplaneMode,
                    onTriggerSync = {
                        // Simulate Delta Sync merge
                        syncQueue = syncQueue.map { it.copy(status = SyncStatus.SYNCED) }
                    }
                )
            }
        }
    }
}
