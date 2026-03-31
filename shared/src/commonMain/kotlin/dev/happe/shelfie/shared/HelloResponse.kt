package dev.happe.shelfie.shared

import kotlinx.serialization.Serializable

@Serializable
data class HelloResponse(val appName: String)
