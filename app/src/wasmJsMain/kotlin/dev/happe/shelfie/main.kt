package dev.happe.shelfie

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import dev.happe.shelfie.data.local.TokenStorage
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val tokenStorage = TokenStorage()
    ComposeViewport(document.body!!) {
        App(tokenStorage)
    }
}
