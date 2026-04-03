package dev.happe.shelfie.di

import android.content.Context
import dev.happe.shelfie.data.local.TokenStorage
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Includes
import dev.zacsweers.metro.Provides

@DependencyGraph
interface AndroidAppGraph : AppGraph {

    @Provides
    fun provideTokenStorage(context: Context): TokenStorage {
        return TokenStorage(context)
    }

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides context: Context,
            @Includes commonBindings: CommonBindings,
        ): AndroidAppGraph
    }
}
