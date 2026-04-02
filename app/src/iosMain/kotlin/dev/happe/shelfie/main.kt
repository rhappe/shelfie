package dev.happe.shelfie

import androidx.compose.ui.window.ComposeUIViewController
import dev.happe.shelfie.data.local.TokenStorage

fun MainViewController() = ComposeUIViewController {
    App(TokenStorage())
}
