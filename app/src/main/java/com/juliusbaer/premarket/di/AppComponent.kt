package com.juliusbaer.premarket.di

import android.app.Application
import com.juliusbaer.premarket.AppApplication
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AndroidSupportInjectionModule::class,
    AppModule::class,
    ActivityBindingModule::class,
    ServiceBindingModule::class,
    ViewModelModule::class,
    AppInitializersModule::class])
interface AppComponent : AndroidInjector<AppApplication>, RealAppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun create(app: Application): Builder

        fun build(): AppComponent
    }
}