package com.learnquest.mp.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
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
import com.learnquest.mp.ui.appStrings
import kotlinx.coroutines.launch

/** The five bottom-navigation tabs. */
sealed class Screen(val route: String, val labelKey: String, val icon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Filled.Home)
    data object Explore : Screen("explore", "Explore", Icons.Filled.Explore)
    data object Learn : Screen("learn", "Learn", Icons.AutoMirrored.Filled.MenuBook)
    data object Progress : Screen("progress", "Guidance", Icons.Filled.School)
    data object Profile : Screen("profile", "Profile", Icons.Filled.Person)

    fun label(language: com.learnquest.mp.model.Language): String = when (this) {
        Home -> language.appStrings().navHome()
        Explore -> language.appStrings().navExplore()
        Learn -> language.appStrings().navLearn()
        Progress -> language.appStrings().navGuidance()
        Profile -> language.appStrings().navProfile()
    }
}

private val tabs = listOf(Screen.Home, Screen.Explore, Screen.Learn, Screen.Progress, Screen.Profile)

@Composable
fun AppNavigation(
    repository: StatefullLearningRepository
) {
    DisposableEffect(repository) {
        onDispose { repository.close() }
    }
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
            onSplashFinished = { showSplash = false },
            appLanguage = appLanguage
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
                    isOffline = isOffline,
                    activeProfileName = activeProfile?.name ?: appLanguage.appStrings().t("Student", "विद्यार्थी", "Student"),
                    xp = activeProfile?.totalXp ?: 0,
                    streakDays = activeProfile?.streakDays ?: 0,
                    isProtected = activeProfile?.isStreakProtected ?: true,
                    pendingSyncCount = syncQueueItems.count { it.status == SyncStatus.PENDING },
                    onSyncClick = { navController.navigate("sync_queue") },
                    appLanguage = appLanguage
                )
                if (currentRoute == Screen.Home.route) {
                    HomeTopBar(
                        studentName = activeProfile?.name ?: "Student",
                        onNotifications = { showMessage(appLanguage.appStrings().t("Notifications: coming soon", "सूचनाएं जल्द उपलब्ध होंगी", "Notifications: soon")) },
                        onProfile = { goToTab(Screen.Profile) },
                        appLanguage = appLanguage
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
                        label = { Text(screen.label(appLanguage)) },
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
                    appLanguage = appLanguage,
                    onNavigate = { route ->
                        when (route) {
                            "explore" -> goToTab(Screen.Explore)
                            "learn" -> goToTab(Screen.Learn)
                            "progress" -> goToTab(Screen.Progress)
                            "profile" -> goToTab(Screen.Profile)
                            "doubt_solver" -> navController.navigate("doubt_solver")
                            "downloads" -> navController.navigate("downloads")
                            "sync_queue" -> navController.navigate("sync_queue")
                            else -> showMessage(route)
                        }
                    }
                )
            }
            composable("downloads") {
                DownloadsScreen(
                    appLanguage = appLanguage,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("doubt_solver") {
                DoubtSolverScreen(
                    isOffline = isOffline,
                    appLanguage = appLanguage,
                    doubtsList = emptyList(),
                    onAskDoubt = { queryText ->
                        repository.addSyncItem("DOUBT_ESCALATE", "Query: $queryText")
                        showMessage(appLanguage.appStrings().t("Query submitted!", "प्रश्न भेज दिया गया!", "Query submit हो गई!"))
                    },
                    onEscalateToTeacher = { doubtId ->
                        repository.addSyncItem("DOUBT_ESCALATE", "Doubt ID: $doubtId")
                        showMessage(appLanguage.appStrings().t("Escalated to local MP teacher queue!", "स्थानीय MP शिक्षक कतार में भेजा गया!", "Local MP teacher queue में भेज दिया!"))
                    }
                )
            }
            composable("sync_queue") {
                SyncQueueScreen(
                    syncQueue = syncQueueItems,
                    isOffline = isOffline,
                    appLanguage = appLanguage,
                    onTriggerSync = {
                        repository.syncAllPending()
                        showMessage(appLanguage.appStrings().t("All pending items synced successfully!", "सभी लंबित आइटम सफलतापूर्वक सिंक हो गए!", "All pending items sync हो गए!"))
                    }
                )
            }
            composable(Screen.Explore.route) {
                ExploreScreen(
                    appLanguage = appLanguage,
                    isOffline = isOffline,
                    onCategoryClick = { category ->
                        showMessage(appLanguage.appStrings().t("Opening $category...", "$category खोला जा रहा है...", "$category open हो रहा है..."))
                    }
                )
            }
            composable(Screen.Learn.route) {
                LearnScreen(
                    questions = quizQuestions,
                    appLanguage = appLanguage,
                    onQuizComplete = { score, mastery ->
                        repository.addSyncItem(
                            actionType = "QUIZ_RESULT",
                            payload = "Quiz Score: $score%, Mastery: ${mastery.name}"
                        )
                        showMessage(appLanguage.appStrings().t("Quiz completed! Mastery: ${appLanguage.appStrings().mastery(mastery)}", "क्विज़ पूरी! स्तर: ${appLanguage.appStrings().mastery(mastery)}", "Quiz complete! Mastery: ${appLanguage.appStrings().mastery(mastery)}"))
                    },
                    onMessage = showMessage
                )
            }
            composable(Screen.Progress.route) {
                OpportunitiesScreen(
                    scholarships = scholarships,
                    careers = careerPathways,
                    appLanguage = appLanguage
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    profiles = profiles,
                    activeProfileId = activeProfileId,
                    selectedAppLanguage = appLanguage,
                    isOffline = isOffline,
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
