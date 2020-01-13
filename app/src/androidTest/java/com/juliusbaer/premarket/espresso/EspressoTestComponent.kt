package com.juliusbaer.premarket.espresso

import android.app.Application
import com.juliusbaer.premarket.TestApplication
import com.juliusbaer.premarket.di.*
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AndroidSupportInjectionModule::class,
    AppModule::class,
    EspressoInitializersModule::class,
    ActivityBindingModule::class,
    ServiceBindingModule::class,
    ViewModelModule::class])
interface EspressoTestComponent: AndroidInjector<TestApplication>, RealAppComponent {
    @Component.Builder
    interface Builder {
        fun appModule(appModule: AppModule): Builder

        @BindsInstance
        fun create(app: Application): Builder

        fun build(): EspressoTestComponent
    }
}
