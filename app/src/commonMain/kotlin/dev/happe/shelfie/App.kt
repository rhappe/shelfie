package dev.happe.shelfie

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import dev.happe.shelfie.data.local.TokenStorage
import dev.happe.shelfie.data.remote.*
import dev.happe.shelfie.data.repository.CategoryRepository
import dev.happe.shelfie.data.repository.PantryRepository
import dev.happe.shelfie.ui.navigation.AppNavigation
import dev.happe.shelfie.viewmodel.AuthViewModel

private const val BASE_URL = "http://localhost:8080"

@Composable
fun App(tokenStorage: TokenStorage) {
    val authApi = remember { AuthApi(HttpClientFactory.createUnauthenticated(), BASE_URL) }
    val authenticatedClient = remember { HttpClientFactory.createAuthenticated(tokenStorage) }
    val categoryApi = remember { CategoryApi(authenticatedClient, BASE_URL) }
    val pantryApi = remember { PantryApi(authenticatedClient, BASE_URL) }
    val categoryRepository = remember { CategoryRepository(categoryApi) }
    val pantryRepository = remember { PantryRepository(pantryApi) }

    MaterialTheme {
        val navController = rememberNavController()
        val authViewModel: AuthViewModel = viewModel { AuthViewModel(tokenStorage, authApi) }
        AppNavigation(
            navController = navController,
            authViewModel = authViewModel,
            categoryRepository = categoryRepository,
            pantryRepository = pantryRepository,
        )
    }
}
