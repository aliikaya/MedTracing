package com.medtracking.app.presentation.share

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtracking.app.domain.model.InvitationLinkResult
import com.medtracking.app.domain.model.MemberRole
import com.medtracking.app.domain.usecase.ShareProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShareProfileViewModel @Inject constructor(
    private val shareProfileUseCase: ShareProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShareProfileUiState())
    val uiState: StateFlow<ShareProfileUiState> = _uiState.asStateFlow()

    fun onRoleSelected(role: MemberRole) {
        _uiState.value = _uiState.value.copy(selectedRole = role, errorMessage = null)
    }

    fun onCreateInvitationClick(profileId: Long) {
        val state = _uiState.value
        if (state.selectedRole == null) {
            _uiState.value = state.copy(errorMessage = "Lütfen bir rol seçin")
            return
        }

        _uiState.value = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val result = shareProfileUseCase(profileId, state.selectedRole)
            when (result) {
                is InvitationLinkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        invitationUrl = result.invitationUrl,
                        showShareSheet = true
                    )
                }
                is InvitationLinkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.reason
                    )
                }
            }
        }
    }

    fun onShareSheetDismissed() {
        _uiState.value = _uiState.value.copy(showShareSheet = false)
    }
}

data class ShareProfileUiState(
    val selectedRole: MemberRole? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val invitationUrl: String? = null,
    val showShareSheet: Boolean = false
)

