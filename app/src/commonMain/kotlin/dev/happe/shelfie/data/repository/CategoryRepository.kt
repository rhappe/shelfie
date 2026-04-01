package dev.happe.shelfie.data.repository

import dev.happe.shelfie.data.remote.ApiClient
import dev.happe.shelfie.shared.*

class CategoryRepository {
    suspend fun getCategories() = ApiClient.getCategories()
    suspend fun createCategory(request: CreateCategoryRequest) = ApiClient.createCategory(request)
    suspend fun updateCategory(id: String, request: UpdateCategoryRequest) = ApiClient.updateCategory(id, request)
    suspend fun deleteCategory(id: String) = ApiClient.deleteCategory(id)
}
