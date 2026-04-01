package dev.happe.shelfie.shared

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String,
)

@Serializable
data class RegisterRequest(
    val username: String,
    val password: String,
    val displayName: String,
    val inviteCode: String? = null,
)

@Serializable
data class AuthResponse(
    val token: String,
    val userId: String,
    val username: String,
    val displayName: String,
    val householdId: String,
)
