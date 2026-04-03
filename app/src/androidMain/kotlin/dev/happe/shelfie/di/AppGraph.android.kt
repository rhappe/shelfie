package dev.happe.shelfie.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dev.zacsweers.metro.createGraphFactory

@Composable
actual fun rememberAppGraph(): AppGraph {
    val context = LocalContext.current
    return remember(context) {
        createGraphFactory<AndroidAppGraph.Factory>().create(context, CommonBindings)
    }
}