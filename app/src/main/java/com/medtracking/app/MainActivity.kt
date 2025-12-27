package com.medtracking.app

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
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
    
    // Deep link için state - onNewIntent'te güncellenecek
    private val pendingDeepLink = mutableStateOf<String?>(null)
    
    @SuppressLint("RememberReturnType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // İlk intent'ten deep link'i al
        extractInviteRoute(intent)?.let { route ->
            pendingDeepLink.value = route
        }

        // Enable edge-to-edge
        enableEdgeToEdge()

        setContent {
            MedTrackingTheme {
                var showSplash by remember { mutableStateOf(true) }
                val currentDeepLink by remember { pendingDeepLink }
                
                // Observe auth state
                val authUser by mainViewModel.authState.collectAsStateWithLifecycle(initialValue = null)

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

                        // Handle invite deep link
                        // Eğer kullanıcı authenticated ise direkt navigate et
                        // Değilse MainViewModel'e pending olarak kaydet (login sonrası işlenecek)
                        LaunchedEffect(showSplash, currentDeepLink, authUser) {
                            val deepLink = currentDeepLink
                            if (!showSplash && deepLink != null) {
                                if (authUser != null) {
                                    // Authenticated - direkt navigate et
                                    navController.navigate(deepLink) {
                                        launchSingleTop = true
                                    }
                                    pendingDeepLink.value = null
                                } else {
                                    // Not authenticated - pending olarak kaydet
                                    mainViewModel.setPendingDeepLink(deepLink)
                                    pendingDeepLink.value = null
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        
        // Yeni deep link geldiğinde handle et
        extractInviteRoute(intent)?.let { route ->
            pendingDeepLink.value = route
        }
    }
    
    /**
     * Intent'ten invitation route'unu çıkarır.
     * Query parametrelerini path parametrelerine dönüştürür.
     * Hem https://medtrack.app/invite hem de medtrack://invite deep linklerini destekler.
     */
    private fun extractInviteRoute(intent: Intent?): String? {
        val data: Uri? = intent?.data
        if (data != null) {
            // HTTPS deep link: https://medtrack.app/invite?invitationId=...&token=...
            if (data.scheme == "https" && data.host == "medtrack.app") {
                val path = data.path
                if (path?.startsWith("/invite") == true) {
                    val invitationId = data.getQueryParameter("invitationId")
                    val token = data.getQueryParameter("token")
                    if (!invitationId.isNullOrBlank() && !token.isNullOrBlank()) {
                        return Screen.Invite.createRoute(
                            invitationId = invitationId,
                            token = token
                        )
                    }
                }
            }
            
            // Custom scheme deep link: medtrack://invite?invitationId=...&token=...
            if (data.scheme == "medtrack" && data.host == "invite") {
                val invitationId = data.getQueryParameter("invitationId")
                val token = data.getQueryParameter("token")
                if (!invitationId.isNullOrBlank() && !token.isNullOrBlank()) {
                    return Screen.Invite.createRoute(
                        invitationId = invitationId,
                        token = token
                    )
                }
            }
        }
        return null
    }
}
