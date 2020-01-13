package com.juliusbaer.premarket.espresso

import com.juliusbaer.premarket.app.DateInitializer
import com.juliusbaer.premarket.app.StatsInitializer
import com.juliusbaer.premarket.core.di.AppInitializer
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet

@Module
abstract class EspressoInitializersModule {
    @Binds
    @IntoSet
    abstract fun providesOkHttpIdlingInitializer(initializer: OkHttpIdlingInitializer): AppInitializer

    @Binds
    @IntoSet
    abstract fun providesDateInitializer(initializer: DateInitializer): AppInitializer

    @Binds
    @IntoSet
    abstract fun providesStatsInitializer(initializer: StatsInitializer): AppInitializer
}