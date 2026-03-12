package com.muzaffer.bistai.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muzaffer.bistai.domain.model.User
import com.muzaffer.bistai.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val user: User? = null,
    val isLoading: Boolean = true, // Uygulama açılışında auth state bekleniyor
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState(isLoading = true))
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.getAuthState().collect { user ->
                _authState.value = AuthState(
                    user = user,
                    isLoading = false,
                    error = null
                )
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            val result = authRepository.signInWithGoogleCredential(idToken)
            result.onFailure { e ->
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "Giriş yapılamadı"
                )
            }
            // Başarılı ise zaten observeAuthState üzerinden user akacak.
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
