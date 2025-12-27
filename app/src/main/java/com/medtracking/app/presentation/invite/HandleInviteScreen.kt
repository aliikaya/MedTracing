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

    // Auth durumunu kontrol et
    LaunchedEffect(authUser, invitationId, token) {
        if (authUser != null && !hasAttemptedAccept) {
            // Authenticated - daveti kabul et
            hasAttemptedAccept = true
            viewModel.acceptInvitation(invitationId, token)
        }
    }

    LaunchedEffect(state) {
        if (state is InviteUiState.Success) {
            // Başarılı - biraz bekle ve yönlendir
            kotlinx.coroutines.delay(1500)
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


