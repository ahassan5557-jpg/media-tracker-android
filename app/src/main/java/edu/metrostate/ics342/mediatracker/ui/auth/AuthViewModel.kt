package edu.metrostate.ics342.mediatracker.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import edu.metrostate.ics342.mediatracker.data.UserRepository
import edu.metrostate.ics342.mediatracker.R

class AuthViewModel : ViewModel() {
    private val repository = UserRepository()

    sealed class AuthUiState {
        object Idle    : AuthUiState()
        object Loading : AuthUiState()
        object Success : AuthUiState()
        data class Error(val msgResId: Int) : AuthUiState()
    }

    // ── Login ─────────────────────────────────────────────────────────────

    private val _email    = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _loginState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val loginState: StateFlow<AuthUiState> = _loginState.asStateFlow()

    fun onEmailChange(value: String)    { _email.value    = value }
    fun onPasswordChange(value: String) { _password.value = value }

    fun onLoginClick() {
        viewModelScope.launch {
            _loginState.value = AuthUiState.Loading
            delay(800)
            if (_email.value.isNotBlank() && _password.value.isNotBlank()) {
                _loginState.value = AuthUiState.Success
            } else {
                _loginState.value = AuthUiState.Error(R.string.error_empty_credentials)            }
        }
    }

    fun resetLoginState() { _loginState.value = AuthUiState.Idle }
    private val _registerState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val registerState: StateFlow<AuthUiState> = _registerState.asStateFlow()

    fun onRegisterClick(
        displayName: String,
        username: String,
        email: String,
        password: String
    ) {
        viewModelScope.launch {
            _registerState.value = AuthUiState.Loading
            try {
                repository.createAccount(
                    displayName = displayName,
                    username = username,
                    email = email,
                    password = password
                )
                _registerState.value = AuthUiState.Success
            } catch (e: Exception) {
                _registerState.value = AuthUiState.Error(R.string.error_empty_credentials)
            }
        }
    }

    fun resetRegisterState() { _registerState.value = AuthUiState.Idle }

}
