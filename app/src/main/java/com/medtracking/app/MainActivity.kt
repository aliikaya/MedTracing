package com.medtracking.app

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.medtracking.app.presentation.navigation.Screen
import com.medtracking.app.presentation.navigation.NavGraph
import com.medtracking.app.presentation.splash.SplashScreen
import com.medtracking.app.ui.theme.MedTrackingTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val mainViewModel: MainViewModel by viewModels()
    
    @SuppressLint("RememberReturnType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge
        enableEdgeToEdge()

        setContent {
            MedTrackingTheme {
                var showSplash by remember { mutableStateOf(true) }
                
                // Observe auth state
                val authUser by mainViewModel.authState.collectAsStateWithLifecycle(initialValue = null)

                // Deeplink'ten gelen davet route'u
                val inviteRoute: String? = remember {
                    val data = intent?.data
                    val invitationId = data?.getQueryParameter("invitationId")
                    val token = data?.getQueryParameter("token")
                    if (!invitationId.isNullOrBlank() && !token.isNullOrBlank()) {
                        Screen.Invite.createRoute(invitationId, token)
                    } else {
                        null
                    }
                }

                // NavController'i en dışta oluştur
                val navController = rememberNavController()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (showSplash) {
                        SplashScreen(
                            onSplashFinished = { showSplash = false }
                        )
                    } else {
                        // Determine start destination based on auth state
                        val startDestination = if (authUser != null) {
                            Screen.Profiles.route
                        } else {
                            Screen.Login.route
                        }
                        
                        NavGraph(
                            navController = navController,
                            startDestination = startDestination
                        )

                        // Handle invite deep link (only if authenticated)
                        LaunchedEffect(inviteRoute, authUser) {
                            if (authUser != null && inviteRoute != null) {
                                navController.navigate(inviteRoute) {
                                    popUpTo(Screen.Profiles.route) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
