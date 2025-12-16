package com.medtracking.app

import androidx.lifecycle.ViewModel
import com.medtracking.app.domain.model.AuthUser
import com.medtracking.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * ViewModel for MainActivity to observe auth state.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val authState: Flow<AuthUser?> = authRepository.observeAuthState()
}

