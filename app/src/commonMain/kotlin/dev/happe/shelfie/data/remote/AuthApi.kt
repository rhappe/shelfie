package dev.happe.shelfie.data.remote

import dev.happe.shelfie.shared.AuthResponse
import dev.happe.shelfie.shared.LoginRequest
import dev.happe.shelfie.shared.RegisterRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class AuthApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun register(request: RegisterRequest): AuthResponse {
        return client.post("$baseUrl/v1/auth/register") {
            setBody(request)
        }.body()
    }

    suspend fun login(request: LoginRequest): AuthResponse {
        return client.post("$baseUrl/v1/auth/login") {
            setBody(request)
        }.body()
    }
}
