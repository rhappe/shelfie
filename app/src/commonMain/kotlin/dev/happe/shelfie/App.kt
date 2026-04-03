package dev.happe.shelfie

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import dev.happe.shelfie.di.AppGraph
import dev.happe.shelfie.di.rememberAppGraph
import dev.happe.shelfie.ui.navigation.AppNavigation
import dev.happe.shelfie.viewmodel.AuthViewModel

@Composable
fun App(graph: AppGraph = rememberAppGraph()) {
    MaterialTheme {
        val navController = rememberNavController()
        val authViewModel: AuthViewModel = viewModel { AuthViewModel(graph) }
        AppNavigation(
            navController = navController,
            graph = graph,
            authViewModel = authViewModel,
        )
    }
}
