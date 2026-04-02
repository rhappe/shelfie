package dev.happe.shelfie

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import dev.happe.shelfie.data.local.TokenStorage
import dev.happe.shelfie.data.remote.ApiClient
import dev.happe.shelfie.data.repository.CategoryRepository
import dev.happe.shelfie.data.repository.PantryRepository
import dev.happe.shelfie.ui.navigation.AppNavigation
import dev.happe.shelfie.viewmodel.AuthViewModel

@Composable
fun App(tokenStorage: TokenStorage) {
    val apiClient = remember { ApiClient(tokenStorage) }
    val categoryRepository = remember { CategoryRepository(apiClient) }
    val pantryRepository = remember { PantryRepository(apiClient) }

    MaterialTheme {
        val navController = rememberNavController()
        val authViewModel: AuthViewModel = viewModel { AuthViewModel(tokenStorage, apiClient) }
        AppNavigation(
            navController = navController,
            authViewModel = authViewModel,
            categoryRepository = categoryRepository,
            pantryRepository = pantryRepository,
        )
    }
}
