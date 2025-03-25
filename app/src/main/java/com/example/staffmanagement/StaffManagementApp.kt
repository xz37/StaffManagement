package com.example.staffmanagement

import androidx.compose.runtime.Composable
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

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable(route = StaffManagementScreen.Login.name) {
            LoginScreen(
                onLoginButtonClicked = {
                    navController.navigate(StaffManagementScreen.StaffDirectory.name)
                }
            )
        }
        composable(route = StaffManagementScreen.StaffDirectory.name) {
            StaffDirectoryScreen(token = "abc123")
        }
    }
}