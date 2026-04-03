package dev.happe.shelfie.data.repository

import dev.happe.shelfie.data.remote.CategoryApi
import dev.happe.shelfie.shared.CreateCategoryRequest
import dev.happe.shelfie.shared.UpdateCategoryRequest

class CategoryRepository(private val categoryApi: CategoryApi) {
    suspend fun getCategories() = categoryApi.getCategories()
    suspend fun createCategory(request: CreateCategoryRequest) = categoryApi.createCategory(request)
    suspend fun updateCategory(id: String, request: UpdateCategoryRequest) = categoryApi.updateCategory(id, request)
    suspend fun deleteCategory(id: String) = categoryApi.deleteCategory(id)
}
