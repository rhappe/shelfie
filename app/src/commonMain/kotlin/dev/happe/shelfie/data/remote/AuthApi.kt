package dev.happe.shelfie.data.remote

import dev.happe.shelfie.shared.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

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
