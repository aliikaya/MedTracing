package com.medtracking.app.presentation.invite

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.medtracking.app.MainViewModel
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

@Composable
fun HandleInviteScreen(
    invitationId: String,
    token: String,
    onSuccessNavigate: () -> Unit,
    onLoginRequired: () -> Unit,
    viewModel: HandleInviteViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val authUser by mainViewModel.authState.collectAsStateWithLifecycle(initialValue = null)
    
    var hasAttemptedAccept by remember { mutableStateOf(false) }

    // #region agent log
    LaunchedEffect(invitationId, token) {
        logDebug("HandleInviteScreen.init", mapOf(
            "invitationId" to invitationId,
            "token" to token.take(20)
        ))
    }
    // #endregion

    // Auth durumunu kontrol et
    LaunchedEffect(authUser, invitationId, token) {
        // #region agent log
        logDebug("HandleInviteScreen.LaunchedEffect.authCheck", mapOf(
            "authUser" to (authUser?.uid ?: "null"),
            "hasAttemptedAccept" to hasAttemptedAccept,
            "invitationId" to invitationId
        ))
        // #endregion
        if (authUser != null && !hasAttemptedAccept) {
            // Authenticated - daveti kabul et
            hasAttemptedAccept = true
            // #region agent log
            logDebug("HandleInviteScreen.acceptInvitation.calling", mapOf(
                "invitationId" to invitationId,
                "token" to token.take(20)
            ))
            // #endregion
            viewModel.acceptInvitation(invitationId, token)
        }
    }

    LaunchedEffect(state) {
        // #region agent log
        logDebug("HandleInviteScreen.LaunchedEffect.state", mapOf(
            "state" to state.javaClass.simpleName,
            "isSuccess" to (state is InviteUiState.Success),
            "isError" to (state is InviteUiState.Error),
            "errorMessage" to (if (state is InviteUiState.Error) (state as InviteUiState.Error).message else null)
        ))
        // #endregion
        if (state is InviteUiState.Success) {
            // Başarılı - Firestore sync için bekle ve yönlendir
            // Firestore'da profile güncellenmesi + snapshot listener'ın tetiklenmesi + local DB'ye yazılması için yeterli süre
            // #region agent log
            logDebug("HandleInviteScreen.Success.waiting", mapOf("delay" to 3500))
            // #endregion
            kotlinx.coroutines.delay(3500)
            // #region agent log
            logDebug("HandleInviteScreen.Success.navigating", mapOf())
            // #endregion
            onSuccessNavigate()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            // Kullanıcı authenticated değil
            authUser == null -> {
                Text(
                    text = "⚠️",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Profil Daveti",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Bu daveti kabul etmek için giriş yapmanız gerekiyor.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onLoginRequired,
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text("Giriş Yap")
                }
            }
            
            // Loading durumu
            state is InviteUiState.Loading -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Davet işleniyor...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Success durumu
            state is InviteUiState.Success -> {
                Text(
                    text = "✓",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Profil Daveti Kabul Edildi!",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Yönlendiriliyorsunuz...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // Error durumu
            state is InviteUiState.Error -> {
                Text(
                    text = "✗",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Hata",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = (state as InviteUiState.Error).message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { 
                        hasAttemptedAccept = false
                        viewModel.acceptInvitation(invitationId, token) 
                    },
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text("Tekrar Dene")
                }
            }
        }
    }
}


