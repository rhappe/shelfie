package dev.happe.shelfie.data.repository

import dev.happe.shelfie.data.remote.ApiClient
import dev.happe.shelfie.shared.*

class CategoryRepository(private val apiClient: ApiClient) {
    suspend fun getCategories() = apiClient.getCategories()
    suspend fun createCategory(request: CreateCategoryRequest) = apiClient.createCategory(request)
    suspend fun updateCategory(id: String, request: UpdateCategoryRequest) = apiClient.updateCategory(id, request)
    suspend fun deleteCategory(id: String) = apiClient.deleteCategory(id)
}
