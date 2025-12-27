package com.medtracking.app

import androidx.lifecycle.ViewModel
import com.medtracking.app.domain.model.AuthUser
import com.medtracking.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * ViewModel for MainActivity to observe auth state and pending deep links.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val authState: Flow<AuthUser?> = authRepository.observeAuthState()
    
    // Pending deep link - login sonrası işlenecek
    private val _pendingDeepLink = MutableStateFlow<String?>(null)
    val pendingDeepLink: StateFlow<String?> = _pendingDeepLink.asStateFlow()
    
    /**
     * Deep link'i bekletmeye al (kullanıcı giriş yapmamışsa)
     */
    fun setPendingDeepLink(route: String?) {
        _pendingDeepLink.value = route
    }
    
    /**
     * Pending deep link'i tüket (navigate edildi)
     */
    fun consumePendingDeepLink(): String? {
        val route = _pendingDeepLink.value
        _pendingDeepLink.value = null
        return route
    }
}

