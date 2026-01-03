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
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val mainViewModel: MainViewModel by viewModels()
    
    @SuppressLint("RememberReturnType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // #region agent log
        logDebug("MainActivity.onCreate", mapOf("intent" to (intent?.data?.toString() ?: "null")))
        // #endregion

        // İlk intent'ten deep link'i al ve MainViewModel'e kaydet
        val route = extractInviteRoute(intent)
        // #region agent log
        logDebug("MainActivity.onCreate.extractRoute", mapOf("route" to (route ?: "null"), "intentData" to (intent?.data?.toString() ?: "null")))
        // #endregion
        route?.let {
            mainViewModel.setPendingDeepLink(it)
            // #region agent log
            logDebug("MainActivity.onCreate.setPendingDeepLink", mapOf("route" to it))
            // #endregion
        }

        // Enable edge-to-edge
        enableEdgeToEdge()

        setContent {
            MedTrackingTheme {
                var showSplash by remember { mutableStateOf(true) }
                
                // Observe auth state
                val authUser by mainViewModel.authState.collectAsStateWithLifecycle(initialValue = null)
                
                // Observe pending deep link from MainViewModel
                val pendingDeepLink by mainViewModel.pendingDeepLink.collectAsStateWithLifecycle()

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
                        // Değilse MainViewModel'de pending olarak zaten kayıtlı (login sonrası NavGraph'te işlenecek)
                        LaunchedEffect(showSplash, pendingDeepLink, authUser) {
                            // #region agent log
                            logDebug("MainActivity.LaunchedEffect.deepLink", mapOf(
                                "showSplash" to showSplash,
                                "pendingDeepLink" to (pendingDeepLink ?: "null"),
                                "authUser" to (authUser?.uid ?: "null")
                            ))
                            // #endregion
                            if (!showSplash && pendingDeepLink != null && authUser != null) {
                                // Authenticated - direkt navigate et
                                // #region agent log
                                logDebug("MainActivity.LaunchedEffect.navigating", mapOf("route" to pendingDeepLink))
                                // #endregion
                                navController.navigate(pendingDeepLink!!) {
                                    launchSingleTop = true
                                }
                                mainViewModel.consumePendingDeepLink()
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
        
        // #region agent log
        logDebug("MainActivity.onNewIntent", mapOf("intent" to (intent?.data?.toString() ?: "null")))
        // #endregion
        
        // Yeni deep link geldiğinde MainViewModel'e kaydet
        val route = extractInviteRoute(intent)
        // #region agent log
        logDebug("MainActivity.onNewIntent.extractRoute", mapOf("route" to (route ?: "null")))
        // #endregion
        route?.let {
            mainViewModel.setPendingDeepLink(it)
            // #region agent log
            logDebug("MainActivity.onNewIntent.setPendingDeepLink", mapOf("route" to it))
            // #endregion
        }
    }
    
    /**
     * Intent'ten invitation route'unu çıkarır.
     * Query parametrelerini path parametrelerine dönüştürür.
     * Hem https://medtrack.app/invite hem de medtrack://invite deep linklerini destekler.
     */
    private fun extractInviteRoute(intent: Intent?): String? {
        val data: Uri? = intent?.data
        // #region agent log
        logDebug("MainActivity.extractInviteRoute.start", mapOf(
            "data" to (data?.toString() ?: "null"),
            "scheme" to (data?.scheme ?: "null"),
            "host" to (data?.host ?: "null"),
            "path" to (data?.path ?: "null"),
            "query" to (data?.query ?: "null"),
            "encodedQuery" to (data?.encodedQuery ?: "null")
        ))
        // #endregion
        if (data != null) {
            // HTTPS deep link: https://medtrack.app/invite?invitationId=...&token=...
            if (data.scheme == "https" && data.host == "medtrack.app") {
                val path = data.path
                if (path?.startsWith("/invite") == true) {
                    val invitationId = data.getQueryParameter("invitationId")
                    val token = data.getQueryParameter("token")
                    // #region agent log
                    logDebug("MainActivity.extractInviteRoute.https", mapOf(
                        "invitationId" to (invitationId ?: "null"),
                        "token" to (token?.take(20) ?: "null")
                    ))
                    // #endregion
                    if (!invitationId.isNullOrBlank() && !token.isNullOrBlank()) {
                        val route = Screen.Invite.createRoute(
                            invitationId = invitationId,
                            token = token
                        )
                        // #region agent log
                        logDebug("MainActivity.extractInviteRoute.https.success", mapOf("route" to route))
                        // #endregion
                        return route
                    }
                }
            }
            
            // Custom scheme deep link: medtrack://invite?invitationId=...&token=...
            if (data.scheme == "medtrack" && data.host == "invite") {
                // Custom scheme'ler için manual parsing yapıyoruz
                // Çünkü getQueryParameter() bazen çalışmıyor
                var invitationId: String? = null
                var token: String? = null
                
                // Önce getQueryParameter ile dene
                invitationId = data.getQueryParameter("invitationId")
                token = data.getQueryParameter("token")
                
                // Eğer null ise, manual parse et
                if (invitationId == null || token == null) {
                    val queryString = data.query ?: data.encodedQuery
                    // #region agent log
                    logDebug("MainActivity.extractInviteRoute.medtrack.manualParse", mapOf(
                        "queryString" to (queryString ?: "null"),
                        "getQueryParam_invitationId" to (invitationId ?: "null"),
                        "getQueryParam_token" to (token ?: "null")
                    ))
                    // #endregion
                    
                    if (!queryString.isNullOrBlank()) {
                        // Query string'i manuel olarak parse et: "invitationId=xxx&token=yyy"
                        val params = queryString.split("&")
                        for (param in params) {
                            val keyValue = param.split("=", limit = 2)
                            if (keyValue.size == 2) {
                                val key = keyValue[0]
                                val value = keyValue[1]
                                when (key) {
                                    "invitationId" -> invitationId = value
                                    "token" -> token = value
                                }
                            }
                        }
                    } else {
                        // Query string yoksa, full URI'den parse et
                        val fullUri = data.toString()
                        val queryStart = fullUri.indexOf('?')
                        if (queryStart != -1) {
                            val queryPart = fullUri.substring(queryStart + 1)
                            val params = queryPart.split("&")
                            for (param in params) {
                                val keyValue = param.split("=", limit = 2)
                                if (keyValue.size == 2) {
                                    val key = keyValue[0]
                                    val value = keyValue[1]
                                    when (key) {
                                        "invitationId" -> invitationId = value
                                        "token" -> token = value
                                    }
                                }
                            }
                        }
                    }
                }
                
                // #region agent log
                logDebug("MainActivity.extractInviteRoute.medtrack", mapOf(
                    "invitationId" to (invitationId ?: "null"),
                    "token" to (token?.take(20) ?: "null")
                ))
                // #endregion
                
                if (!invitationId.isNullOrBlank() && !token.isNullOrBlank()) {
                    val route = Screen.Invite.createRoute(
                        invitationId = invitationId,
                        token = token
                    )
                    // #region agent log
                    logDebug("MainActivity.extractInviteRoute.medtrack.success", mapOf("route" to route))
                    // #endregion
                    return route
                }
            }
        }
        // #region agent log
        logDebug("MainActivity.extractInviteRoute.returnNull", mapOf())
        // #endregion
        return null
    }
    
    // #region agent log
    private fun logDebug(location: String, data: Map<String, Any?>) {
        val dataStr = data.entries.joinToString(", ") { "${it.key}=${it.value}" }
        android.util.Log.d("DebugLog", "$location: $dataStr")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val logFile = File("/Users/alikaya/Projeler/MadTracking/.cursor/debug.log")
                val logLine = "{\"timestamp\":${System.currentTimeMillis()},\"location\":\"$location\",\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"message\":\"Debug log\",\"data\":{$dataStr}}\n"
                logFile.appendText(logLine)
            } catch (e: Exception) {
                android.util.Log.e("DebugLog", "Error writing log file: ${e.message}")
            }
        }
    }
    // #endregion
}
