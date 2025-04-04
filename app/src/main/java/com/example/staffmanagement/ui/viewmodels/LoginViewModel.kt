package com.example.staffmanagement.ui.viewmodels

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.staffmanagement.data.NetworkLoginRepository
import com.example.staffmanagement.domain.LoginResult
import com.example.staffmanagement.network.NetworkApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val isLoginSuccessful: Boolean = false,
    val token: String = ""
)

class LoginViewModel : ViewModel() {
    private val repository = NetworkLoginRepository(NetworkApiService())

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val emailPattern = Regex(
        "[a-zA-Z0-9+._%\\-]{1,256}" +
                "@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
    )
    private val passwordPattern = Regex("^[a-zA-Z0-9]{6,10}$")

    val isEmailValid: Boolean
        get() = checkEmail(_uiState.value.email)

    val isPasswordValid: Boolean
        get() = checkPassword(_uiState.value.password)

    val isFormValid: Boolean
        get() = isEmailValid && isPasswordValid

    @VisibleForTesting
    fun checkEmail(email: String): Boolean {
        return email.isNotEmpty() && emailPattern.matches(email)
    }

    @VisibleForTesting
    fun checkPassword(password: String): Boolean {
        return password.isNotEmpty() && passwordPattern.matches(password)
    }

    fun updateEmail(email: String) {
        _uiState.update { currentState ->
            currentState.copy(email = email)
        }
    }

    fun updatePassword(password: String) {
        _uiState.update { currentState ->
            currentState.copy(password = password)
        }
    }

    fun resetLoginState() {
        _uiState.update { currentState ->
            currentState.copy(
                isLoginSuccessful = false,
                token = ""
            )
        }
    }

    fun login() {
        if (!isFormValid) return

        _uiState.update { currentState ->
            currentState.copy(isLoading = true, errorMessage = "")
        }

        viewModelScope.launch {
            try {
                when (val result = repository.login(_uiState.value.email, _uiState.value.password)) {
                    is LoginResult.Success -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                isLoginSuccessful = true,
                                token = result.token
                            )
                        }
                    }
                    is LoginResult.Error -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                errorMessage = result.message
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        errorMessage = "Network error: ${e.message ?: "Unknown error"}"
                    )
                }
            }
        }
    }
}