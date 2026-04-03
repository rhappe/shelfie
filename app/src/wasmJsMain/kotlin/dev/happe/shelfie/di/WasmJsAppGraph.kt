package dev.happe.shelfie.di

import dev.happe.shelfie.data.local.TokenStorage
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Includes
import dev.zacsweers.metro.Provides

@DependencyGraph
interface WasmJsAppGraph : AppGraph {

    @Provides
    fun provideTokenStorage(): TokenStorage {
        return TokenStorage()
    }

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Includes commonBindings: CommonBindings): WasmJsAppGraph
    }
}
