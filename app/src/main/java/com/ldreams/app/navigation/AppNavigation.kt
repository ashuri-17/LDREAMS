package com.ldreams.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ldreams.app.ui.screens.AddDreamScreen
import com.ldreams.app.ui.screens.DashboardScreen
import com.ldreams.app.ui.screens.DreamAnalysisScreen
import com.ldreams.app.ui.screens.DreamDetailScreen
import com.ldreams.app.ui.screens.DreamJournalScreen
import com.ldreams.app.ui.screens.GuidesScreen
import com.ldreams.app.ui.screens.LucidProgramScreen
import com.ldreams.app.ui.screens.LucidityTrackerScreen
import com.ldreams.app.ui.screens.RealityCheckScreen
import com.ldreams.app.ui.screens.SettingsScreen

data class BottomNavItem(
    val label: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem("Home", "dashboard", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem("Journal", "journal", Icons.Filled.Book, Icons.Outlined.Book),
    BottomNavItem("Analysis", "analysis", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    BottomNavItem("Lucidity", "lucidity", Icons.Filled.Psychology, Icons.Outlined.Psychology),
    BottomNavItem("Settings", "settings", Icons.Filled.Settings, Icons.Outlined.BarChart)
)

// ---- Navigation transition helpers ----

private fun AnimatedContentTransitionScope<NavBackStackEntry>.slideEnter(): EnterTransition {
    return slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Up,
        animationSpec = tween(400)
    ) + fadeIn(animationSpec = tween(400))
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.slideExit(): ExitTransition {
    return slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Down,
        animationSpec = tween(400)
    ) + fadeOut(animationSpec = tween(400))
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.fadeEnter(): EnterTransition {
    return fadeIn(animationSpec = tween(400))
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.fadeExit(): ExitTransition {
    return fadeOut(animationSpec = tween(400))
}

/** Routes that are drill-down sub-pages (not bottom-nav tabs) */
private val drillDownRoutes = setOf(
    "add_dream", "dream_detail/{dreamId}", "reality_checks", "guides", "lucid_program"
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val navRoutes = listOf("dashboard", "journal", "analysis", "lucidity", "settings")

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
                tonalElevation = 0.dp
            ) {
                bottomNavItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            navController.navigate(item.route) {
                                popUpTo("dashboard") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == index) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(paddingValues)
        ) {
            // ---- Tab screens: simple fade transitions ----
            composable(
                "dashboard",
                enterTransition = { fadeEnter() },
                exitTransition = { fadeExit() }
            ) {
                DashboardScreen(navController = navController)
            }
            composable(
                "journal",
                enterTransition = { fadeEnter() },
                exitTransition = { fadeExit() }
            ) {
                DreamJournalScreen(navController = navController)
            }
            composable(
                "analysis",
                enterTransition = { fadeEnter() },
                exitTransition = { fadeExit() }
            ) {
                DreamAnalysisScreen(navController = navController)
            }
            composable(
                "lucidity",
                enterTransition = { fadeEnter() },
                exitTransition = { fadeExit() }
            ) {
                LucidityTrackerScreen(navController = navController)
            }
            composable(
                "settings",
                enterTransition = { fadeEnter() },
                exitTransition = { fadeExit() }
            ) {
                SettingsScreen(navController = navController)
            }

            // ---- Drill-down screens: slide-up/slide-down + fade ----
            composable(
                "add_dream",
                enterTransition = { slideEnter() },
                exitTransition = { slideExit() },
                popEnterTransition = { fadeEnter() },
                popExitTransition = { fadeExit() }
            ) {
                AddDreamScreen(navController = navController)
            }
            composable(
                "dream_detail/{dreamId}",
                enterTransition = { slideEnter() },
                exitTransition = { slideExit() },
                popEnterTransition = { fadeEnter() },
                popExitTransition = { fadeExit() }
            ) { backStackEntry ->
                val dreamId = backStackEntry.arguments?.getString("dreamId")?.toLongOrNull() ?: 0L
                DreamDetailScreen(dreamId = dreamId, navController = navController)
            }
            composable(
                "reality_checks",
                enterTransition = { slideEnter() },
                exitTransition = { slideExit() },
                popEnterTransition = { fadeEnter() },
                popExitTransition = { fadeExit() }
            ) {
                RealityCheckScreen(navController = navController)
            }
            composable(
                "guides",
                enterTransition = { slideEnter() },
                exitTransition = { slideExit() },
                popEnterTransition = { fadeEnter() },
                popExitTransition = { fadeExit() }
            ) {
                GuidesScreen(navController = navController)
            }
            composable(
                "lucid_program",
                enterTransition = { slideEnter() },
                exitTransition = { slideExit() },
                popEnterTransition = { fadeEnter() },
                popExitTransition = { fadeExit() }
            ) {
                LucidProgramScreen(navController = navController)
            }
        }
    }
}
