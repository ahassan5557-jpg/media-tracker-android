package edu.metrostate.ics342.mediatracker.ui.auth


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.metrostate.ics342.mediatracker.data.UserRepository
import edu.metrostate.ics342.mediatracker.data.RegisterResult

import edu.metrostate.ics342.mediatracker.data.network.DefaultUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val userRepository: UserRepository = DefaultUserRepository()
) : ViewModel() {
    sealed class RegisterUiState {
        data object Idle    : RegisterUiState()
        data object Loading : RegisterUiState()
        data object Success : RegisterUiState()
        data class Error(val msgResId: Int) : RegisterUiState()
    }

    private val _displayName = MutableStateFlow("")
    val displayName = _displayName.asStateFlow()x

    fun setDisplayName(newValue: String) {
        _displayName.value = newValue
    }
    fun onRegisterClick() {
        viewModelScope.launch {
            _registerState.value = RegisterUiState.Loading
            if (_displayName.value.isBlank() || _email.value.isBlank() ||
                _username.value.isBlank() || _password.value.isBlank() ||
                _confirmPassword.value.isBlank()
            ) {
                _registerState.value = RegisterUiState.Error(R.string.error_empty_fields)
                return@launch
            }

            if (_password.value != _confirmPassword.value) {
                _registerState.value = RegisterUiState.Error(R.string.error_passwords_mismatch)
                return@launch
            }

            val result = userRepository.register(
                email       = _email.value,
                password    = _password.value,
                username    = _username.value,
                displayName = _displayName.value
            )

            _registerState.value = when (result) {
                RegisterResult.Success      -> RegisterUiState.Success
                RegisterResult.Conflict     -> RegisterUiState.Error(R.string.error_email_or_username_taken)
                RegisterResult.NetworkError -> RegisterUiState.Error(R.string.error_network)
                RegisterResult.UnknownError -> RegisterUiState.Error(R.string.error_generic)
            }
        }
    }

    fun onSignUpClicked() {
        viewModelScope.launch {
            userRepository.createAccount("", "","","")
        }
    }
}