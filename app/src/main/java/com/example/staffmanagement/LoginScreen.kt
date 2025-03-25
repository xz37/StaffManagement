package com.example.staffmanagement

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL


@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    val emailPattern = remember { Regex("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+") }
    val passwordPattern = remember { Regex("^[a-zA-Z0-9]{6,10}$") }

    val isEmailValid = email.isNotEmpty() && emailPattern.matches(email)
    val isPasswordValid = password.isNotEmpty() && passwordPattern.matches(password)
    val isFormValid = isEmailValid && isPasswordValid

    suspend fun login(email: String, password: String): Pair<Boolean, String> {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("https://reqres.in/api/login")
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

    fun handleLoginButtonClick() {
        isLoading = true
        errorMessage = ""

        coroutineScope.launch {
            try {
                val result = login(email, password)
                isLoading = false
                if (result.first) {
                    onLoginSuccess(result.second)
                } else {
                    errorMessage = result.second
                }
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "Network error: ${e.message ?: "Unknown error"}"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Staff Management Login",
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            isError = email.isNotEmpty() && !emailPattern.matches(email),
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            isError = password.isNotEmpty() && !passwordPattern.matches(password),
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { handleLoginButtonClick() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = isFormValid && !isLoading,
        ) {
            Text(
                text = "Login",
            )
        }

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Snackbar(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(errorMessage)
            }
        }
    }

}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(onLoginSuccess = {})
}