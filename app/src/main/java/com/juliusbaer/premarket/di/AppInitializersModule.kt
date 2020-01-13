package com.juliusbaer.premarket.di

import com.juliusbaer.premarket.app.DateInitializer
import com.juliusbaer.premarket.app.DebugInitializer
import com.juliusbaer.premarket.app.StatsInitializer
import com.juliusbaer.premarket.core.di.AppInitializer
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet

@Module
abstract class AppInitializersModule {
    @Binds
    @IntoSet
    abstract fun providesDateInitializer(initializer: DateInitializer): AppInitializer

    @Binds
    @IntoSet
    abstract fun providesDebugInitializer(initializer: DebugInitializer): AppInitializer

    @Binds
    @IntoSet
    abstract fun providesStatsInitializer(initializer: StatsInitializer): AppInitializer
}