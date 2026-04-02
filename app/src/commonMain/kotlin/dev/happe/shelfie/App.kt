package dev.happe.shelfie

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import dev.happe.shelfie.data.local.TokenStorage
import dev.happe.shelfie.di.AppGraph
import dev.happe.shelfie.ui.navigation.AppNavigation
import dev.happe.shelfie.viewmodel.AuthViewModel
import dev.zacsweers.metro.createGraphFactory

@Composable
fun App(tokenStorage: TokenStorage) {
    val graph = remember { createGraphFactory<AppGraph.Factory>().create(tokenStorage) }

    MaterialTheme {
        val navController = rememberNavController()
        val authViewModel: AuthViewModel = viewModel {
            AuthViewModel(graph.tokenStorage, graph.authApi)
        }
        AppNavigation(
            navController = navController,
            authViewModel = authViewModel,
            categoryRepository = graph.categoryRepository,
            pantryRepository = graph.pantryRepository,
        )
    }
}
