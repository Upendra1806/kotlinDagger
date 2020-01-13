package com.juliusbaer.premarket.app

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.facebook.stetho.Stetho
import com.juliusbaer.premarket.core.di.AppInitializer
import com.uphyca.stetho_realm.RealmInspectorModulesProvider
import timber.log.Timber
import javax.inject.Inject

class DebugInitializer @Inject constructor(): AppInitializer {
    override fun init(application: Application) {
        Timber.plant(object : Timber.DebugTree() {
            override fun createStackElementTag(element: StackTraceElement): String {
                return super.createStackElementTag(element) + "::Line:" + element.lineNumber + "::" + element.methodName + "()"
            }
        })
        WorkManager.initialize(
                application,
                Configuration.Builder().build())

        Stetho.initialize(
                Stetho.newInitializerBuilder(application)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(application))
                        .enableWebKitInspector(RealmInspectorModulesProvider.builder(application)
                                .withLimit(500)
                                .build())
                        .build())
    }
}
