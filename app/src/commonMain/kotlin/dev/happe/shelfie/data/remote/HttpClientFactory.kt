package dev.happe.shelfie.data.remote

import dev.happe.shelfie.data.local.TokenStorage
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object HttpClientFactory {
    private val jsonConfig = Json { ignoreUnknownKeys = true; isLenient = true }

    fun createUnauthenticated(): HttpClient {
        return HttpClient {
            install(ContentNegotiation) { json(jsonConfig) }
            defaultRequest { contentType(ContentType.Application.Json) }
            installResponseValidator()
        }
    }

    fun createAuthenticated(tokenStorage: TokenStorage): HttpClient {
        return HttpClient {
            install(ContentNegotiation) { json(jsonConfig) }
            defaultRequest {
                contentType(ContentType.Application.Json)
                tokenStorage.getToken()?.let {
                    headers.append(HttpHeaders.Authorization, "Bearer $it")
                }
            }
            installResponseValidator()
        }
    }

    private fun HttpClientConfig<*>.installResponseValidator() {
        HttpResponseValidator {
            validateResponse { response ->
                if (!response.status.isSuccess()) {
                    val body = response.bodyAsText()
                    throw RuntimeException("${response.status}: $body")
                }
            }
        }
    }
}
