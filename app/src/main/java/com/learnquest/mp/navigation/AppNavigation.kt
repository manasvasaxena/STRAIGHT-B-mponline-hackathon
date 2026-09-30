package com.learnquest.mp.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.learnquest.mp.data.repository.StatefullLearningRepository
import com.learnquest.mp.model.SyncStatus
import com.learnquest.mp.ui.components.StartupSplashScreen
import com.learnquest.mp.ui.components.TopStatusHeader
import com.learnquest.mp.ui.screens.*
import kotlinx.coroutines.launch

/** The five bottom-navigation tabs. */
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Filled.Home)
    data object Explore : Screen("explore", "Explore", Icons.Filled.Explore)
    data object Learn : Screen("learn", "Learn", Icons.AutoMirrored.Filled.MenuBook)
    data object Progress : Screen("progress", "Progress", Icons.Filled.EmojiEvents)
    data object Profile : Screen("profile", "Profile", Icons.Filled.Person)
}

private val tabs = listOf(Screen.Home, Screen.Explore, Screen.Learn, Screen.Progress, Screen.Profile)

@Composable
fun AppNavigation(
    repository: StatefullLearningRepository = remember { StatefullLearningRepository() }
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val isOffline by repository.isOfflineFlow.collectAsState()
    val appLanguage by repository.appLanguageFlow.collectAsState()
    val profiles by repository.profilesFlow.collectAsState()
    val activeProfileId by repository.activeProfileIdFlow.collectAsState()
    val scholarships by repository.scholarshipsFlow.collectAsState()
    val careerPathways by repository.careerPathwaysFlow.collectAsState()
    val quizQuestions by repository.quizQuestionsFlow.collectAsState()
    val syncQueueItems by repository.syncQueueFlow.collectAsState()
    val homeData = remember(activeProfileId, profiles) { repository.getHomeData() }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Screen.Home.route

    val showMessage: (String) -> Unit = { message ->
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message)
        }
    }

    fun goToTab(screen: Screen) {
        navController.navigate(screen.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    var showSplash by remember { mutableStateOf(true) }

    if (showSplash) {
        StartupSplashScreen(
            onSplashFinished = { showSplash = false }
        )
        return
    }

    val topInsetPadding = WindowInsets.statusBars.asPaddingValues()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(modifier = Modifier.padding(top = topInsetPadding.calculateTopPadding())) {
                val activeProfile = profiles.find { it.id == activeProfileId } ?: profiles.firstOrNull()
                TopStatusHeader(
                    isAirplaneMode = isOffline,
                    onToggleAirplaneMode = { repository.toggleOfflineMode() },
                    activeProfileName = activeProfile?.name ?: "Student",
                    xp = activeProfile?.totalXp ?: 0,
                    streakDays = activeProfile?.streakDays ?: 0,
                    isProtected = activeProfile?.isStreakProtected ?: true,
                    pendingSyncCount = syncQueueItems.count { it.status == SyncStatus.PENDING },
                    onSyncClick = { navController.navigate("sync_queue") }
                )
                if (currentRoute == Screen.Home.route) {
                    HomeTopBar(
                        studentName = activeProfile?.name ?: "Student",
                        onNotifications = { showMessage("Notifications: coming soon") },
                        onProfile = { goToTab(Screen.Profile) }
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar {
                tabs.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = { goToTab(screen) },
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.label) },
                        alwaysShowLabel = true
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    data = homeData,
                    isOffline = isOffline,
                    onMessage = { msg ->
                        if (msg.contains("Ask AI") || msg.contains("Doubt")) {
                            navController.navigate("doubt_solver")
                        } else if (msg.contains("Sync")) {
                            navController.navigate("sync_queue")
                        } else {
                            showMessage(msg)
                        }
                    }
                )
            }
            composable("doubt_solver") {
                DoubtSolverScreen(
                    isOffline = isOffline,
                    doubtsList = emptyList(),
                    onAskDoubt = { queryText ->
                        repository.addSyncItem("DOUBT_ESCALATE", "Query: $queryText")
                        showMessage("Query submitted!")
                    },
                    onEscalateToTeacher = { doubtId ->
                        repository.addSyncItem("DOUBT_ESCALATE", "Doubt ID: $doubtId")
                        showMessage("Escalated to local MP teacher queue!")
                    }
                )
            }
            composable("sync_queue") {
                SyncQueueScreen(
                    syncQueue = syncQueueItems,
                    isOffline = isOffline,
                    onTriggerSync = {
                        repository.syncAllPending()
                        showMessage("All pending items synced successfully!")
                    }
                )
            }
            composable(Screen.Explore.route) {
                ExploreScreen(
                    appLanguage = appLanguage,
                    onCategoryClick = { category ->
                        showMessage("Opening $category...")
                    }
                )
            }
            composable(Screen.Learn.route) {
                QuizScreen(
                    questions = quizQuestions,
                    onQuizComplete = { score, mastery ->
                        repository.addSyncItem(
                            actionType = "QUIZ_RESULT",
                            payload = "Quiz Score: $score%, Mastery: ${mastery.name}"
                        )
                        showMessage("Quiz completed! Mastery: ${mastery.label}")
                    }
                )
            }
            composable(Screen.Progress.route) {
                OpportunitiesScreen(
                    scholarships = scholarships,
                    careers = careerPathways
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    profiles = profiles,
                    activeProfileId = activeProfileId,
                    selectedAppLanguage = appLanguage,
                    isOffline = isOffline,
                    onToggleOffline = { repository.toggleOfflineMode() },
                    onSelectProfile = { repository.selectProfile(it) },
                    onSelectLanguage = { repository.setAppLanguage(it) },
                    onCreateProfile = { name, grade, lang ->
                        repository.addProfile(name, grade, lang)
                    }
                )
            }
        }
    }
}
