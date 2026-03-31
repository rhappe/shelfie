package dev.happe.shelfie.data.remote

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*

const val BASE_URL = "http://localhost:8080"

val apiClient = HttpClient {
    install(ContentNegotiation) {
        json()
    }
}
