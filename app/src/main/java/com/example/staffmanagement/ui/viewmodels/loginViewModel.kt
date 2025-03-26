package com.example.staffmanagement.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val isLoginSuccessful: Boolean = false,
    val token: String = ""
)

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val emailPattern = Regex("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")
    private val passwordPattern = Regex("^[a-zA-Z0-9]{6,10}$")

    val isEmailValid: Boolean
        get() = _uiState.value.email.isNotEmpty() && emailPattern.matches(_uiState.value.email)

    val isPasswordValid: Boolean
        get() = _uiState.value.password.isNotEmpty() && passwordPattern.matches(_uiState.value.password)

    val isFormValid: Boolean
        get() = isEmailValid && isPasswordValid

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

    fun login() {
        if (!isFormValid) return

        _uiState.update { currentState ->
            currentState.copy(isLoading = true, errorMessage = "")
        }

        viewModelScope.launch {
            try {
                val result = performLogin(_uiState.value.email, _uiState.value.password)
                if (result.first) {
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            isLoginSuccessful = true,
                            token = result.second
                        )
                    }
                } else {
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            errorMessage = result.second
                        )
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

    private suspend fun performLogin(email: String, password: String): Pair<Boolean, String> {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("https://reqres.in/api/login?delay=5")
                val connection = url.openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val jsonPayload = JSONObject().apply {
                    put("email", email)
                    put("password", password)
                }

                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(jsonPayload.toString())
                    writer.flush()
                }

                val responseCode = connection.responseCode

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    connection.inputStream.bufferedReader().use { reader ->
                        val response = reader.readText()
                        val jsonResponse = JSONObject(response)
                        val token = jsonResponse.getString("token")
                        Pair(true, token)
                    }
                } else {
                    connection.errorStream?.bufferedReader()?.use { reader ->
                        val errorResponse = reader.readText()
                        try {
                            val jsonError = JSONObject(errorResponse)
                            val error = jsonError.getString("error")
                            Pair(false, error)
                        } catch (e: Exception) {
                            Pair(false, "Error: $errorResponse")
                        }
                    } ?: Pair(false, "Error: HTTP $responseCode")
                }
            } catch (e: Exception) {
                Pair(false, "Connection error: ${e.message ?: "Unknown error"}")
            }
        }
    }
}