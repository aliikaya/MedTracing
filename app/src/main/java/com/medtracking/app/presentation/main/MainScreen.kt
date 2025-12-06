package com.medtracking.app.presentation.main

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.medtracking.app.presentation.addmedication.AddMedicationWizardScreen
import com.medtracking.app.presentation.history.MedicationHistoryScreen
import com.medtracking.app.presentation.medications.MedicationsScreen
import com.medtracking.app.presentation.navigation.BottomNavItem
import com.medtracking.app.presentation.navigation.Screen
import com.medtracking.app.presentation.profile.ProfileDetailScreen
import com.medtracking.app.presentation.today.TodayScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    profileId: Long,
    onNavigateBack: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // Bottom bar'ı gösterecek route'lar
    val showBottomBar = currentDestination?.route in listOf(
        BottomNavItem.Today.route,
        BottomNavItem.Medications.route,
        BottomNavItem.History.route,
        BottomNavItem.Profile.route
    )

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = NavigationBarDefaults.Elevation
                ) {
                    (BottomNavItem.items ?: emptyList()).filterNotNull().forEach { item ->
                        val itemRoute = item.route
                        val selected = currentDestination?.hierarchy?.any { destination ->
                            destination?.route == itemRoute
                        } == true
                        
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(itemRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            },
                            label = { 
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.labelSmall
                                ) 
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Today.route,
            modifier = Modifier.padding(paddingValues),
            enterTransition = {
                fadeIn(animationSpec = tween(300)) + slideInHorizontally(
                    initialOffsetX = { it / 4 },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                    targetOffsetX = { -it / 4 },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(300)) + slideInHorizontally(
                    initialOffsetX = { -it / 4 },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                    targetOffsetX = { it / 4 },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                )
            }
        ) {
            // Today Tab
            composable(
                route = BottomNavItem.Today.route,
                enterTransition = { fadeIn(tween(200)) },
                exitTransition = { fadeOut(tween(200)) }
            ) {
                TodayScreen(
                    profileId = profileId,
                    onNavigateToAddMedication = {
                        navController.navigate("add_medication")
                    },
                    onNavigateToMedicationHistory = { medicationId ->
                        navController.navigate("medication_history/$medicationId")
                    }
                )
            }
            
            // Medications Tab
            composable(
                route = BottomNavItem.Medications.route,
                enterTransition = { fadeIn(tween(200)) },
                exitTransition = { fadeOut(tween(200)) }
            ) {
                MedicationsScreen(
                    profileId = profileId,
                    onNavigateToAddMedication = {
                        navController.navigate("add_medication")
                    },
                    onNavigateToMedicationHistory = { medicationId ->
                        navController.navigate("medication_history/$medicationId")
                    }
                )
            }
            
            // History Tab
            composable(
                route = BottomNavItem.History.route,
                enterTransition = { fadeIn(tween(200)) },
                exitTransition = { fadeOut(tween(200)) }
            ) {
                HistoryTabScreen(
                    profileId = profileId,
                    onNavigateToMedicationHistory = { medicationId ->
                        navController.navigate("medication_history/$medicationId")
                    }
                )
            }
            
            // Profile Tab
            composable(
                route = BottomNavItem.Profile.route,
                enterTransition = { fadeIn(tween(200)) },
                exitTransition = { fadeOut(tween(200)) }
            ) {
                ProfileDetailScreen(
                    profileId = profileId,
                    onNavigateBack = onNavigateBack
                )
            }
            
            // Add Medication Wizard (detail screen - no bottom bar)
            composable(
                route = "add_medication",
                enterTransition = {
                    slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(400, easing = FastOutSlowInEasing)
                    ) + fadeIn(tween(400))
                },
                exitTransition = {
                    slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                    ) + fadeOut(tween(300))
                },
                popEnterTransition = { fadeIn(tween(200)) },
                popExitTransition = {
                    slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                    ) + fadeOut(tween(300))
                }
            ) {
                AddMedicationWizardScreen(
                    profileId = profileId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            // Medication History (detail screen - no bottom bar)
            composable(
                route = "medication_history/{medicationId}",
                arguments = listOf(navArgument("medicationId") { type = NavType.LongType }),
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                    ) + fadeIn(tween(300))
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                    ) + fadeOut(tween(300))
                },
                popEnterTransition = { fadeIn(tween(200)) },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                    ) + fadeOut(tween(300))
                }
            ) {
                MedicationHistoryScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
private fun HistoryTabScreen(
    profileId: Long,
    onNavigateToMedicationHistory: (Long) -> Unit
) {
    // Bu bir placeholder - gerçek geçmiş ekranı için MedicationHistoryScreen kullanılacak
    // Ama şimdilik burada genel bir geçmiş özeti gösterelim
    Box(modifier = Modifier.fillMaxSize()) {
        com.medtracking.app.ui.components.EmptyState(
            icon = "📊",
            title = "Geçmiş",
            subtitle = "İlaç kartlarından geçmişi görebilirsiniz"
        )
    }
}

