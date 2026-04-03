package dev.happe.shelfie.di

import androidx.compose.runtime.Composable
import dev.happe.shelfie.data.local.TokenStorage
import dev.happe.shelfie.data.remote.AuthApi
import dev.happe.shelfie.data.repository.CategoryRepository
import dev.happe.shelfie.data.repository.PantryRepository

interface AppGraph {
    val tokenStorage: TokenStorage
    val authApi: AuthApi
    val categoryRepository: CategoryRepository
    val pantryRepository: PantryRepository
}

@Composable
expect fun rememberAppGraph(): AppGraph
