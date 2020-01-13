package com.juliusbaer.premarket.espresso

import android.os.AsyncTask
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.WorkManager
import com.juliusbaer.premarket.TestApplication
import com.juliusbaer.premarket.core.utils.CoroutinesDispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class EspressoClassRule: TestRule {
    override fun apply(base: Statement, description: Description?) = MyStatement(base)

    class MyStatement(private val base: Statement) : Statement() {
        @Throws(Throwable::class)
        override fun evaluate() {
            //init async routines with espresso compatible AsyncTask.THREAD_POOL_EXECUTOR
            CoroutinesDispatchers.IO = AsyncTask.THREAD_POOL_EXECUTOR.asCoroutineDispatcher()

            val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TestApplication
            WorkManager.initialize(
                    app,
                    Configuration.Builder()
                            .setExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                            .setTaskExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                            .build())

            val component = DaggerEspressoTestComponent
                    .builder()
                    .create(app)
                    .build()
            app.injectIfNeeded(component)

            try {
                base.evaluate() // This executes your tests
            } finally {
                // Add something you do after test
            }
        }
    }
}