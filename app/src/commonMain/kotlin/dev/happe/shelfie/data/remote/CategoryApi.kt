package dev.happe.shelfie.data.remote

import dev.happe.shelfie.shared.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class CategoryApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getCategories(): List<Category> {
        return client.get("$baseUrl/v1/categories").body()
    }

    suspend fun createCategory(request: CreateCategoryRequest): Category {
        return client.post("$baseUrl/v1/categories") {
            setBody(request)
        }.body()
    }

    suspend fun updateCategory(id: String, request: UpdateCategoryRequest): Category {
        return client.put("$baseUrl/v1/categories/$id") {
            setBody(request)
        }.body()
    }

    suspend fun deleteCategory(id: String) {
        client.delete("$baseUrl/v1/categories/$id")
    }
}
