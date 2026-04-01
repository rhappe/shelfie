package dev.happe.shelfie

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import dev.happe.shelfie.ui.navigation.AppNavigation
import dev.happe.shelfie.viewmodel.AuthViewModel

@Composable
fun App() {
    MaterialTheme {
        val navController = rememberNavController()
        val authViewModel: AuthViewModel = viewModel { AuthViewModel() }
        AppNavigation(navController = navController, authViewModel = authViewModel)
    }
}
