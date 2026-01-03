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
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@HiltViewModel
class HandleInviteViewModel @Inject constructor(
    private val acceptProfileInvitationUseCase: AcceptProfileInvitationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<InviteUiState>(InviteUiState.Loading)
    val uiState: StateFlow<InviteUiState> = _uiState

    fun acceptInvitation(invitationId: String, token: String) {
        viewModelScope.launch {
            // #region agent log
            logDebug("HandleInviteViewModel.acceptInvitation.start", mapOf(
                "invitationId" to invitationId,
                "token" to token.take(20)
            ))
            // #endregion
            _uiState.value = InviteUiState.Loading
            val result = acceptProfileInvitationUseCase(invitationId, token)
            // #region agent log
            logDebug("HandleInviteViewModel.acceptInvitation.result", mapOf(
                "result" to result.javaClass.simpleName,
                "isSuccess" to (result is AcceptInvitationResult.Success),
                "errorMessage" to (if (result is AcceptInvitationResult.Error) result.message else null)
            ))
            // #endregion
            when (result) {
                AcceptInvitationResult.Success -> {
                    _uiState.value = InviteUiState.Success
                    // #region agent log
                    logDebug("HandleInviteViewModel.acceptInvitation.success", mapOf())
                    // #endregion
                }
                is AcceptInvitationResult.Error -> {
                    _uiState.value = InviteUiState.Error(result.message)
                    // #region agent log
                    logDebug("HandleInviteViewModel.acceptInvitation.error", mapOf("message" to result.message))
                    // #endregion
                }
            }
        }
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


