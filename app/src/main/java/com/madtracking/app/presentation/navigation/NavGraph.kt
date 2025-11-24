package com.madtracking.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.madtracking.app.presentation.profiles.ProfilesScreen
import com.madtracking.app.presentation.today.TodayScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Profiles.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Profiles.route) {
            ProfilesScreen(
                onProfileClick = { profileId ->
                    navController.navigate(Screen.Today.createRoute(profileId))
                }
            )
        }

        composable(
            route = Screen.Today.route,
            arguments = listOf(
                navArgument("profileId") { type = NavType.StringType }
            )
        ) {
            TodayScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

