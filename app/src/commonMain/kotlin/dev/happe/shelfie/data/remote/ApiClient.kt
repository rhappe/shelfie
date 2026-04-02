package dev.happe.shelfie.data.remote

import dev.happe.shelfie.data.local.TokenStorage
import dev.happe.shelfie.shared.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class ApiClient(private val tokenStorage: TokenStorage) {
    var baseUrl: String = "http://localhost:8080"

    private val jsonConfig = Json { ignoreUnknownKeys = true; isLenient = true }

    private val unauthenticatedClient = HttpClient {
        install(ContentNegotiation) { json(jsonConfig) }
        defaultRequest { contentType(ContentType.Application.Json) }
        HttpResponseValidator {
            validateResponse { response ->
                if (!response.status.isSuccess()) {
                    val body = response.bodyAsText()
                    throw RuntimeException("${response.status}: $body")
                }
            }
        }
    }

    private val authenticatedClient = HttpClient {
        install(ContentNegotiation) { json(jsonConfig) }
        defaultRequest {
            contentType(ContentType.Application.Json)
            tokenStorage.getToken()?.let {
                headers.append(HttpHeaders.Authorization, "Bearer $it")
            }
        }
        HttpResponseValidator {
            validateResponse { response ->
                if (!response.status.isSuccess()) {
                    val body = response.bodyAsText()
                    throw RuntimeException("${response.status}: $body")
                }
            }
        }
    }

    // Auth
    suspend fun register(request: RegisterRequest): AuthResponse {
        return unauthenticatedClient.post("$baseUrl/v1/auth/register") {
            setBody(request)
        }.body()
    }

    suspend fun login(request: LoginRequest): AuthResponse {
        return unauthenticatedClient.post("$baseUrl/v1/auth/login") {
            setBody(request)
        }.body()
    }

    // Categories
    suspend fun getCategories(): List<Category> {
        return authenticatedClient.get("$baseUrl/v1/categories").body()
    }

    suspend fun createCategory(request: CreateCategoryRequest): Category {
        return authenticatedClient.post("$baseUrl/v1/categories") {
            setBody(request)
        }.body()
    }

    suspend fun updateCategory(id: String, request: UpdateCategoryRequest): Category {
        return authenticatedClient.put("$baseUrl/v1/categories/$id") {
            setBody(request)
        }.body()
    }

    suspend fun deleteCategory(id: String) {
        authenticatedClient.delete("$baseUrl/v1/categories/$id")
    }

    // Pantry Items
    suspend fun getPantryItems(
        search: String? = null,
        categoryId: String? = null,
        sortBy: String? = null,
    ): List<PantryItem> {
        return authenticatedClient.get("$baseUrl/v1/pantry/items") {
            search?.let { parameter("search", it) }
            categoryId?.let { parameter("categoryId", it) }
            sortBy?.let { parameter("sortBy", it) }
        }.body()
    }

    suspend fun createPantryItem(request: CreatePantryItemRequest): PantryItem {
        return authenticatedClient.post("$baseUrl/v1/pantry/items") {
            setBody(request)
        }.body()
    }

    suspend fun updatePantryItem(id: String, request: UpdatePantryItemRequest): PantryItem {
        return authenticatedClient.put("$baseUrl/v1/pantry/items/$id") {
            setBody(request)
        }.body()
    }

    suspend fun deletePantryItem(id: String) {
        authenticatedClient.delete("$baseUrl/v1/pantry/items/$id")
    }
}
