package dev.happe.shelfie.shared

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String,
)

@Serializable
data class AuthResponse(
    val token: String,
    val userId: String,
    val email: String,
    val displayName: String,
    val householdId: String,
)
