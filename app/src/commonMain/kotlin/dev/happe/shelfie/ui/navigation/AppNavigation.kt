package dev.happe.shelfie.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import dev.happe.shelfie.di.AppGraph
import dev.happe.shelfie.ui.screens.AddEditPantryItemScreen
import dev.happe.shelfie.ui.screens.CategoryScreen
import dev.happe.shelfie.ui.screens.LoginScreen
import dev.happe.shelfie.ui.screens.PantryScreen
import dev.happe.shelfie.ui.screens.RegisterScreen
import dev.happe.shelfie.viewmodel.AddEditItemViewModel
import dev.happe.shelfie.viewmodel.AuthViewEvent
import dev.happe.shelfie.viewmodel.AuthViewModel
import dev.happe.shelfie.viewmodel.AuthViewState
import dev.happe.shelfie.viewmodel.CategoryViewModel
import dev.happe.shelfie.viewmodel.PantryViewModel

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Pantry : Screen("pantry")
    data object AddPantryItem : Screen("pantry/add")
    data object EditPantryItem : Screen("pantry/edit/{itemId}") {
        fun createRoute(itemId: String) = "pantry/edit/$itemId"
    }
    data object Categories : Screen("categories")
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    graph: AppGraph,
    authViewModel: AuthViewModel,
) {
    val authState by authViewModel.viewState.collectAsStateWithLifecycle()

    val isAuthenticated = authState is AuthViewState.Content && (authState as AuthViewState.Content).isAuthenticated
    val startDestination = if (isAuthenticated) Screen.Pantry.route else Screen.Login.route

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Pantry.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                authViewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.Pantry.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                },
            )
        }

        composable(Screen.Pantry.route) {
            PantryScreen(
                viewModel = viewModel { PantryViewModel(graph) },
                onAddItem = { navController.navigate(Screen.AddPantryItem.route) },
                onEditItem = { itemId -> navController.navigate(Screen.EditPantryItem.createRoute(itemId)) },
                onNavigateToCategories = { navController.navigate(Screen.Categories.route) },
                onLogout = {
                    authViewModel.handleEvent(AuthViewEvent.Logout)
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.AddPantryItem.route) {
            AddEditPantryItemScreen(
                viewModel = viewModel { AddEditItemViewModel(graph) },
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Screen.EditPantryItem.route,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val itemId: String = backStackEntry.savedStateHandle.get<String>("itemId")
                ?: return@composable
            AddEditPantryItemScreen(
                viewModel = viewModel { AddEditItemViewModel(graph, itemId) },
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(Screen.Categories.route) {
            CategoryScreen(
                viewModel = viewModel { CategoryViewModel(graph) },
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
