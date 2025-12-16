package com.medtracking.app.presentation.invite

sealed class InviteUiState {
    object Loading : InviteUiState()
    object Success : InviteUiState()
    data class Error(val message: String) : InviteUiState()
}


