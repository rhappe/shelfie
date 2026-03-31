package dev.happe.shelfie

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import dev.happe.shelfie.data.remote.BASE_URL
import dev.happe.shelfie.data.remote.apiClient
import dev.happe.shelfie.shared.HelloResponse
import io.ktor.client.call.*
import io.ktor.client.request.*

@Composable
fun App() {
    var appName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val response = apiClient.get("$BASE_URL/v1/hello").body<HelloResponse>()
            appName = response.appName
        } catch (e: Exception) {
            appName = "error: ${e.message}"
        }
    }

    MaterialTheme {
        Surface {
            Text(if (appName != null) "Hello, $appName!" else "Loading…")
        }
    }
}
