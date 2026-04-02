package dev.happe.shelfie.data.repository

import dev.happe.shelfie.data.remote.ApiClient
import dev.happe.shelfie.shared.*

class PantryRepository(private val apiClient: ApiClient) {
    suspend fun getItems(
        search: String? = null,
        categoryId: String? = null,
        sortBy: String? = null,
    ): List<PantryItem> {
        return apiClient.getPantryItems(search, categoryId, sortBy)
    }

    suspend fun createItem(request: CreatePantryItemRequest) = apiClient.createPantryItem(request)
    suspend fun updateItem(id: String, request: UpdatePantryItemRequest) = apiClient.updatePantryItem(id, request)
    suspend fun deleteItem(id: String) = apiClient.deletePantryItem(id)
}
