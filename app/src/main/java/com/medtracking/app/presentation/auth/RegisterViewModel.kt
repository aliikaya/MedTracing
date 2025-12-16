package com.medtracking.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtracking.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email, errorMessage = null)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, errorMessage = null)
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = confirmPassword, errorMessage = null)
    }

    fun onRegisterClick() {
        val state = _uiState.value

        // Validation
        if (state.email.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Email gerekli")
            return
        }
        if (state.password.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Şifre gerekli")
            return
        }
        if (state.password.length < 6) {
            _uiState.value = state.copy(errorMessage = "Şifre en az 6 karakter olmalı")
            return
        }
        if (state.password != state.confirmPassword) {
            _uiState.value = state.copy(errorMessage = "Şifreler eşleşmiyor")
            return
        }

        _uiState.value = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val result = authRepository.register(state.email, state.password)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = parseErrorMessage(error.message)
                    )
                }
            )
        }
    }

    private fun parseErrorMessage(message: String?): String {
        return when {
            message?.contains("InvalidEmail") == true -> "Geçersiz email adresi"
            message?.contains("EmailAlreadyInUse") == true -> "Bu email zaten kullanımda"
            message?.contains("WeakPassword") == true -> "Şifre çok zayıf"
            message?.contains("NetworkError") == true -> "Ağ hatası"
            else -> "Kayıt başarısız: ${message ?: "Bilinmeyen hata"}"
        }
    }
}

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

