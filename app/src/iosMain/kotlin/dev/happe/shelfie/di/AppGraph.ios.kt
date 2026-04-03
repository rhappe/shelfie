package dev.happe.shelfie.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.zacsweers.metro.createGraphFactory

@Composable
actual fun rememberAppGraph(): AppGraph {
    return remember {
        createGraphFactory<IosAppGraph.Factory>().create(CommonBindings)
    }
}