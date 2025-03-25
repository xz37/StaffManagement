package com.example.staffmanagement

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


enum class StaffManagementScreen() {
    Login,
    StaffDirectory,
}

@Composable
fun StaffManagementApp(
    navController: NavHostController = rememberNavController()
) {
    val loginToken = remember { mutableStateOf("") }

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable(route = StaffManagementScreen.Login.name) {
            LoginScreen(
                onLoginSuccess = { token ->
                    loginToken.value = token
                    navController.navigate(StaffManagementScreen.StaffDirectory.name)
                }
            )
        }
        composable(route = StaffManagementScreen.StaffDirectory.name) {
            StaffDirectoryScreen(token = loginToken.value)
        }
    }
}