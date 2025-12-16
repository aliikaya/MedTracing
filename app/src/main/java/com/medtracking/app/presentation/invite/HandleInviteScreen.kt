package com.medtracking.app.presentation.invite

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun HandleInviteScreen(
    invitationId: String,
    token: String,
    onSuccessNavigate: () -> Unit,
    viewModel: HandleInviteViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(invitationId, token) {
        viewModel.acceptInvitation(invitationId, token)
    }

    LaunchedEffect(state) {
        if (state is InviteUiState.Success) {
            onSuccessNavigate()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (state) {
            InviteUiState.Loading -> {
                CircularProgressIndicator()
            }

            InviteUiState.Success -> {
                Text(
                    text = "Profil daveti kabul edildi",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            is InviteUiState.Error -> {
                Text(
                    text = (state as InviteUiState.Error).message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
                Button(onClick = { viewModel.acceptInvitation(invitationId, token) }) {
                    Text("Tekrar dene")
                }
            }
        }
    }
}


