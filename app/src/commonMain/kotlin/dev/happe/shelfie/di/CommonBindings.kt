package dev.happe.shelfie.di

import dev.happe.shelfie.data.local.TokenStorage
import dev.happe.shelfie.data.remote.AuthApi
import dev.happe.shelfie.data.remote.CategoryApi
import dev.happe.shelfie.data.remote.HttpClientFactory
import dev.happe.shelfie.data.remote.PantryApi
import dev.happe.shelfie.data.repository.CategoryRepository
import dev.happe.shelfie.data.repository.PantryRepository
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Provides
import io.ktor.client.HttpClient

private const val BASE_URL = "http://10.0.2.2:8080"

@BindingContainer
object CommonBindings {

    @Provides
    @UnauthenticatedClient
    fun provideUnauthenticatedClient(): HttpClient {
        return HttpClientFactory.createUnauthenticated()
    }

    @Provides
    @AuthenticatedClient
    fun provideAuthenticatedClient(tokenStorage: TokenStorage): HttpClient {
        return HttpClientFactory.createAuthenticated(tokenStorage)
    }

    @Provides
    fun provideAuthApi(@UnauthenticatedClient client: HttpClient): AuthApi {
        return AuthApi(client, BASE_URL)
    }

    @Provides
    fun provideCategoryApi(@AuthenticatedClient client: HttpClient): CategoryApi {
        return CategoryApi(client, BASE_URL)
    }

    @Provides
    fun providePantryApi(@AuthenticatedClient client: HttpClient): PantryApi {
        return PantryApi(client, BASE_URL)
    }

    @Provides
    fun provideCategoryRepository(categoryApi: CategoryApi): CategoryRepository {
        return CategoryRepository(categoryApi)
    }

    @Provides
    fun providePantryRepository(pantryApi: PantryApi): PantryRepository {
        return PantryRepository(pantryApi)
    }
}
