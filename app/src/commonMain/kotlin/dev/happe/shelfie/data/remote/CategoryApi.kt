package dev.happe.shelfie.data.remote

import dev.happe.shelfie.shared.Category
import dev.happe.shelfie.shared.CreateCategoryRequest
import dev.happe.shelfie.shared.UpdateCategoryRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody

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
