package dev.happe.shelfie.data.repository

import dev.happe.shelfie.data.remote.PantryApi
import dev.happe.shelfie.shared.CreatePantryItemRequest
import dev.happe.shelfie.shared.PantryItem
import dev.happe.shelfie.shared.UpdatePantryItemRequest

class PantryRepository(private val pantryApi: PantryApi) {
    suspend fun getItems(
        search: String? = null,
        categoryId: String? = null,
        sortBy: String? = null,
    ): List<PantryItem> {
        return pantryApi.getPantryItems(search, categoryId, sortBy)
    }

    suspend fun createItem(request: CreatePantryItemRequest) = pantryApi.createPantryItem(request)
    suspend fun updateItem(id: String, request: UpdatePantryItemRequest) = pantryApi.updatePantryItem(id, request)
    suspend fun deleteItem(id: String) = pantryApi.deletePantryItem(id)
}
