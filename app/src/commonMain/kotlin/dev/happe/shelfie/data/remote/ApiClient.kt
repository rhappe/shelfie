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

object ApiClient {
    var baseUrl: String = "http://localhost:8080"

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
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

    private fun HttpRequestBuilder.withAuth() {
        TokenStorage.getToken()?.let { headers.append(HttpHeaders.Authorization, "Bearer $it") }
    }

    // Auth
    suspend fun register(request: RegisterRequest): AuthResponse =
        client.post("$baseUrl/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun login(request: LoginRequest): AuthResponse =
        client.post("$baseUrl/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    // Categories
    suspend fun getCategories(): List<Category> =
        client.get("$baseUrl/v1/categories") { withAuth() }.body()

    suspend fun createCategory(request: CreateCategoryRequest): Category =
        client.post("$baseUrl/v1/categories") {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun updateCategory(id: String, request: UpdateCategoryRequest): Category =
        client.put("$baseUrl/v1/categories/$id") {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun deleteCategory(id: String) {
        client.delete("$baseUrl/v1/categories/$id") { withAuth() }
    }

    // Pantry Items
    suspend fun getPantryItems(
        search: String? = null,
        categoryId: String? = null,
        sortBy: String? = null,
    ): List<PantryItem> =
        client.get("$baseUrl/v1/pantry/items") {
            withAuth()
            search?.let { parameter("search", it) }
            categoryId?.let { parameter("categoryId", it) }
            sortBy?.let { parameter("sortBy", it) }
        }.body()

    suspend fun createPantryItem(request: CreatePantryItemRequest): PantryItem =
        client.post("$baseUrl/v1/pantry/items") {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun updatePantryItem(id: String, request: UpdatePantryItemRequest): PantryItem =
        client.put("$baseUrl/v1/pantry/items/$id") {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun deletePantryItem(id: String) {
        client.delete("$baseUrl/v1/pantry/items/$id") { withAuth() }
    }
}
