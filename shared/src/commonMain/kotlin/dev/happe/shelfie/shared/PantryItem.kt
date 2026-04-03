package dev.happe.shelfie.shared

import kotlinx.serialization.Serializable

@Serializable
data class PantryItem(
    val id: String,
    val name: String,
    val quantity: Double,
    val unit: String,
    val categoryId: String? = null,
    val expirationDate: String? = null,
    val lowStockThreshold: Double = 0.0,
    val notifyOnLowStock: Boolean = true,
    val barcode: String? = null,
    val householdId: String,
    val createdAt: String,
    val updatedAt: String,
)
