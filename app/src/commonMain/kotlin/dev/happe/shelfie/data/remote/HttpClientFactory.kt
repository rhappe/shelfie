package dev.happe.shelfie.data.remote

import dev.happe.shelfie.data.local.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
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
                val token = tokenStorage.getToken()
                if (!token.isNullOrEmpty()) {
                    bearerAuth(token)
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
