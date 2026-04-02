package dev.happe.shelfie.data.remote

import dev.happe.shelfie.shared.CreatePantryItemRequest
import dev.happe.shelfie.shared.PantryItem
import dev.happe.shelfie.shared.UpdatePantryItemRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody

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
