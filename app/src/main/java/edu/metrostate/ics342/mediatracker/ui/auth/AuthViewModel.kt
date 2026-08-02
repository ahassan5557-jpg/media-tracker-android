package edu.metrostate.ics342.mediatracker.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

import edu.metrostate.ics342.mediatracker.R
import edu.metrostate.ics342.mediatracker.data.network.LoginResult
import edu.metrostate.ics342.mediatracker.data.network.SessionRepository
import edu.metrostate.ics342.mediatracker.data.network.UserRepository
import edu.metrostate.ics342.mediatracker.data.datastore.DefaultSessionRepository
import edu.metrostate.ics342.mediatracker.data.network.DefaultUserRepository
import edu.metrostate.ics342.mediatracker.data.network.RegisterResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepository: UserRepository = DefaultUserRepository()
    private val sessionRepository: SessionRepository = DefaultSessionRepository(application)

    sealed class AuthUiState {
        data object Idle    : AuthUiState()
        data object Loading : AuthUiState()
        data object Success : AuthUiState()
        data class Error(val msgResId: Int) : AuthUiState()
    }

    // ── Login ─────────────────────────────────────────────────────────────

    private val _email    = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _loginState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val loginState: StateFlow<AuthUiState> = _loginState.asStateFlow()
    private val _displayName = MutableStateFlow("")
    val displayName: StateFlow<String> = _displayName.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    fun onDisplayNameChange(value: String) { _displayName.value = value }
    fun onUsernameChange(value: String)    { _username.value    = value }

    fun onEmailChange(value: String)    { _email.value    = value }
    fun onPasswordChange(value: String) { _password.value = value }

    fun onLoginClick() {
        viewModelScope.launch {
            _loginState.value = AuthUiState.Loading
            if (_email.value.isBlank() || _password.value.isBlank()) {
                _loginState.value = AuthUiState.Error(R.string.error_empty_credentials)
                return@launch
            }

            val result = userRepository.login(
                email    = _email.value,
                password = _password.value
            )

            when (result) {
                is LoginResult.Success -> {
                    sessionRepository.saveSession(
                        accessToken  = result.accessToken,
                        refreshToken = result.refreshToken,
                        user         = result.user
                    )
                    _loginState.value = AuthUiState.Success
                }
                LoginResult.InvalidCredentials -> _loginState.value = AuthUiState.Error(R.string.error_invalid_credentials)
                LoginResult.NetworkError       -> _loginState.value = AuthUiState.Error(R.string.error_network)
                LoginResult.UnknownError       -> _loginState.value = AuthUiState.Error(R.string.error_generic)

                        }
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

            val result = userRepository.register(
                email = email,
                password = password,
                username = username,
                displayName = displayName
            )

            when (result) {
                RegisterResult.Success      -> _registerState.value = AuthUiState.Success
                RegisterResult.Conflict     -> _registerState.value = AuthUiState.Error(R.string.error_conflict) // add this string resource if it doesn't exist
                RegisterResult.NetworkError -> _registerState.value = AuthUiState.Error(R.string.error_network)
                RegisterResult.UnknownError -> _registerState.value = AuthUiState.Error(R.string.error_generic)
            }
        }
    }

    fun resetRegisterState() { _registerState.value = AuthUiState.Idle }

}
