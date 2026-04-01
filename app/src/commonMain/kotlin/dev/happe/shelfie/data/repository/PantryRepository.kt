package dev.happe.shelfie.data.repository

import dev.happe.shelfie.data.remote.ApiClient
import dev.happe.shelfie.shared.*

class PantryRepository {
    suspend fun getItems(
        search: String? = null,
        categoryId: String? = null,
        sortBy: String? = null,
    ) = ApiClient.getPantryItems(search, categoryId, sortBy)

    suspend fun createItem(request: CreatePantryItemRequest) = ApiClient.createPantryItem(request)
    suspend fun updateItem(id: String, request: UpdatePantryItemRequest) = ApiClient.updatePantryItem(id, request)
    suspend fun deleteItem(id: String) = ApiClient.deletePantryItem(id)
}
