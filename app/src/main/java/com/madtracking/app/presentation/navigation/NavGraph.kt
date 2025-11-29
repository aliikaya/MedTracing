package com.madtracking.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.madtracking.app.presentation.addmedication.AddMedicationScreen
import com.madtracking.app.presentation.history.MedicationHistoryScreen
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
        // Profiller ekranı
        composable(Screen.Profiles.route) {
            ProfilesScreen(
                onProfileClick = { profileId ->
                    navController.navigate(Screen.Today.createRoute(profileId))
                },
                onAddProfile = {
                    // Şimdilik basit - ileride AddProfileScreen'e yönlendir
                }
            )
        }

        // Bugünkü ilaçlar ekranı
        composable(
            route = Screen.Today.route,
            arguments = listOf(
                navArgument("profileId") { type = NavType.StringType }
            )
        ) {
            TodayScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAddMedication = { profileId ->
                    navController.navigate(Screen.AddMedication.createRoute(profileId))
                },
                onNavigateToMedicationHistory = { medicationId ->
                    navController.navigate(Screen.MedicationHistory.createRoute(medicationId))
                }
            )
        }

        // İlaç ekleme ekranı
        composable(
            route = Screen.AddMedication.route,
            arguments = listOf(
                navArgument("profileId") { type = NavType.StringType }
            )
        ) {
            AddMedicationScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // İlaç geçmişi ekranı
        composable(
            route = Screen.MedicationHistory.route,
            arguments = listOf(
                navArgument("medicationId") { type = NavType.StringType }
            )
        ) {
            MedicationHistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
