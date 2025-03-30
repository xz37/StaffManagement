package com.example.staffmanagement.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.staffmanagement.R
import com.example.staffmanagement.ui.viewmodels.LoginViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    loginViewModel: LoginViewModel = viewModel()
) {
    val loginUiState by loginViewModel.uiState.collectAsState()

    if (loginUiState.isLoginSuccessful && loginUiState.token.isNotEmpty()) {
        onLoginSuccess(loginUiState.token)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.staff_management_login),
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = loginUiState.email,
                onValueChange = { loginViewModel.updateEmail(it) },
                label = { Text(stringResource(R.string.email)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                isError = loginUiState.email.isNotEmpty() && !loginViewModel.isEmailValid,
                supportingText = {
                    if (loginUiState.email.isNotEmpty() && !loginViewModel.isEmailValid) {
                        Text(
                            text = "Please enter a valid email address",
                            color = Color.Red
                        )
                    }
                },
                singleLine = true,
            )

            OutlinedTextField(
                value = loginUiState.password,
                onValueChange = { loginViewModel.updatePassword(it) },
                label = { Text(stringResource(R.string.password)) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                isError = loginUiState.password.isNotEmpty() && !loginViewModel.isPasswordValid,
                supportingText = {
                    if (loginUiState.password.isNotEmpty() && !loginViewModel.isPasswordValid) {
                        Text(
                            text = "Password must be 6-10 alphanumeric characters",
                            color = Color.Red
                        )
                    }
                },
                singleLine = true,
                )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { loginViewModel.login() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = loginViewModel.isFormValid && !loginUiState.isLoading,
            ) {
                if (loginUiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(30.dp),
                            color = Color.Gray
                        )
                    }
                } else {
                    Text(
                        text = stringResource(R.string.login),
                    )
                }
            }
        }

        if (loginUiState.errorMessage.isNotEmpty()) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text(loginUiState.errorMessage)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(onLoginSuccess = {})
}