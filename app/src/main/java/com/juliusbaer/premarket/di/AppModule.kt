package com.juliusbaer.premarket.di

import android.app.Application
import android.content.Context
import com.juliusbaer.premarket.dataFlow.DataManger
import com.juliusbaer.premarket.dataFlow.IDataManager
import com.juliusbaer.premarket.dataFlow.IUserStorage
import com.juliusbaer.premarket.dataFlow.database.DbHelper
import com.juliusbaer.premarket.dataFlow.database.IDbHelper
import com.juliusbaer.premarket.dataFlow.network.Api
import dagger.Module
import dagger.Provides
import io.realm.Realm
import io.realm.RealmConfiguration
import org.zeromq.ZContext
import javax.inject.Singleton

@Module(includes = [NetworkModule::class, StorageModule::class])
object AppModule {
    @JvmStatic
    @Provides
    fun provideContext(app: Application): Context = app.applicationContext

    @JvmStatic
    @Provides
    @Singleton
    fun provideDataManager(api: Api, storageModule: IUserStorage, database: IDbHelper): IDataManager = DataManger(api, storageModule, database)

    @JvmStatic
    @Singleton
    @Provides
    fun provideZContext(): ZContext = ZContext(1)

    @JvmStatic
    @Singleton
    @Provides
    fun provideDbManager(context: Context): IDbHelper {
        Realm.init(context)
        val config = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .schemaVersion(11)
                .build()
        Realm.setDefaultConfiguration(config)
        val db = DbHelper()
        db.clearCaches()
        return db
    }
}