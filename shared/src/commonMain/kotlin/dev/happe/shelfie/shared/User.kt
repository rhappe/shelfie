package dev.happe.shelfie.shared

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val username: String,
    val displayName: String,
    val householdId: String,
    val role: String,
    val createdAt: String,
)
