package com.learnquest.mp.navigation

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
import androidx.compose.foundation.layout.padding
import com.learnquest.mp.data.repository.LearningRepository
import com.learnquest.mp.data.repository.MockLearningRepository
import com.learnquest.mp.ui.screens.HomeScreen
import com.learnquest.mp.ui.screens.HomeTopBar
import com.learnquest.mp.ui.screens.PlaceholderScreen
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
    // Swap this for a real repository later; screens will not need to change.
    repository: LearningRepository = remember { MockLearningRepository() }
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val homeData = remember { repository.getHomeData() }
    val isOffline = remember { repository.isOffline() }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Screen.Home.route

    // Shows a short message at the bottom ("Coming soon" placeholders).
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // Only the Home tab has the custom top bar for now.
            if (currentRoute == Screen.Home.route) {
                HomeTopBar(
                    studentName = homeData.progress.studentName,
                    onNotifications = { showMessage("Notifications: coming soon") },
                    onProfile = { goToTab(Screen.Profile) }
                )
            }
        },
        bottomBar = {
            NavigationBar {
                tabs.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = { goToTab(screen) },
                        icon = { Icon(screen.icon, contentDescription = null) }, // label below describes it
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
                HomeScreen(data = homeData, isOffline = isOffline, onMessage = showMessage)
            }
            composable(Screen.Explore.route) { PlaceholderScreen("Explore", "🗺") }
            composable(Screen.Learn.route) { PlaceholderScreen("Learn", "📚") }
            composable(Screen.Progress.route) { PlaceholderScreen("Progress", "🏆") }
            composable(Screen.Profile.route) { PlaceholderScreen("Profile", "👤") }
        }
    }
}
