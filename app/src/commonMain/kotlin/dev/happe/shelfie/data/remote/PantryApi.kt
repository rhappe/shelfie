package dev.happe.shelfie.data.remote

import dev.happe.shelfie.shared.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class PantryApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getPantryItems(
        search: String? = null,
        categoryId: String? = null,
        sortBy: String? = null,
    ): List<PantryItem> {
        return client.get("$baseUrl/v1/pantry/items") {
            search?.let { parameter("search", it) }
            categoryId?.let { parameter("categoryId", it) }
            sortBy?.let { parameter("sortBy", it) }
        }.body()
    }

    suspend fun createPantryItem(request: CreatePantryItemRequest): PantryItem {
        return client.post("$baseUrl/v1/pantry/items") {
            setBody(request)
        }.body()
    }

    suspend fun updatePantryItem(id: String, request: UpdatePantryItemRequest): PantryItem {
        return client.put("$baseUrl/v1/pantry/items/$id") {
            setBody(request)
        }.body()
    }

    suspend fun deletePantryItem(id: String) {
        client.delete("$baseUrl/v1/pantry/items/$id")
    }
}
