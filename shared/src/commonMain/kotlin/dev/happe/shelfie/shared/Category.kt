package dev.happe.shelfie.shared

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: String,
    val name: String,
    val description: String? = null,
    val color: String? = null,
    val sortOrder: Int = 0,
    val householdId: String,
)
