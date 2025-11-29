package com.madtracking.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.madtracking.app.presentation.main.MainScreen
import com.madtracking.app.presentation.profiles.ProfilesScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Profiles.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Profiller ekranı (başlangıç noktası)
        composable(Screen.Profiles.route) {
            ProfilesScreen(
                onProfileClick = { profileId ->
                    navController.navigate(Screen.Main.createRoute(profileId))
                },
                onAddProfile = {
                    // Şimdilik basit - AddProfileScreen'e yönlendir
                }
            )
        }

        // Ana ekran (bottom nav ile)
        composable(
            route = Screen.Main.route,
            arguments = listOf(
                navArgument("profileId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getLong("profileId") ?: 0L
            MainScreen(
                profileId = profileId,
                onNavigateBack = {
                    navController.popBackStack(Screen.Profiles.route, inclusive = false)
                }
            )
        }
    }
}
