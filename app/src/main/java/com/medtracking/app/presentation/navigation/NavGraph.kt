package com.medtracking.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.medtracking.app.MainViewModel
import com.medtracking.app.presentation.auth.LoginScreen
import com.medtracking.app.presentation.auth.RegisterScreen
import com.medtracking.app.presentation.invite.HandleInviteScreen
import com.medtracking.app.presentation.main.MainScreen
import com.medtracking.app.presentation.profiles.ProfilesScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val pendingDeepLink by mainViewModel.pendingDeepLink.collectAsStateWithLifecycle()
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth screens
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
                    // Pending deep link varsa oraya git, yoksa profiles'a git
                    val deepLink = mainViewModel.consumePendingDeepLink()
                    if (deepLink != null) {
                        navController.navigate(deepLink) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Profiles.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    // Pending deep link varsa oraya git, yoksa profiles'a git
                    val deepLink = mainViewModel.consumePendingDeepLink()
                    if (deepLink != null) {
                        navController.navigate(deepLink) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Profiles.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        // Main app screens
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

        composable(
            route = Screen.Invite.route,
            arguments = listOf(
                navArgument("invitationId") { type = NavType.StringType },
                navArgument("token") { type = NavType.StringType }
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "https://medtrack.app/invite?invitationId={invitationId}&token={token}"
                },
                navDeepLink {
                    uriPattern = "medtrack://invite?invitationId={invitationId}&token={token}"
                }
            )
        ) { backStackEntry ->
            val invitationId = backStackEntry.arguments?.getString("invitationId") ?: return@composable
            val token = backStackEntry.arguments?.getString("token") ?: return@composable
            // #region agent log
            android.util.Log.d("NavGraph", "HandleInviteScreen composable called with invitationId=$invitationId, token=${token.take(20)}")
            // #endregion
            HandleInviteScreen(
                invitationId = invitationId,
                token = token,
                onSuccessNavigate = {
                    navController.navigate(Screen.Profiles.route) {
                        popUpTo(Screen.Profiles.route) { inclusive = true }
                    }
                },
                onLoginRequired = {
                    // Kullanıcı authenticated değilse login'e yönlendir
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
