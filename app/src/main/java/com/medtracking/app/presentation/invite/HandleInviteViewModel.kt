package com.medtracking.app.presentation.invite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtracking.app.domain.usecase.AcceptProfileInvitationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.medtracking.app.domain.model.AcceptInvitationResult

@HiltViewModel
class HandleInviteViewModel @Inject constructor(
    private val acceptProfileInvitationUseCase: AcceptProfileInvitationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<InviteUiState>(InviteUiState.Loading)
    val uiState: StateFlow<InviteUiState> = _uiState

    fun acceptInvitation(invitationId: String, token: String) {
        viewModelScope.launch {
            _uiState.value = InviteUiState.Loading
            when (val result = acceptProfileInvitationUseCase(invitationId, token)) {
                AcceptInvitationResult.Success -> _uiState.value = InviteUiState.Success
                is AcceptInvitationResult.Error -> _uiState.value = InviteUiState.Error(result.message)
            }
        }
    }
}


