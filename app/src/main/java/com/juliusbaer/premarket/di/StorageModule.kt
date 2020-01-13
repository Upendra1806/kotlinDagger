package com.juliusbaer.premarket.di

import android.content.Context
import com.juliusbaer.premarket.dataFlow.IUserStorage
import com.juliusbaer.premarket.dataFlow.PreferenceUserStorage
import com.juliusbaer.premarket.helpers.NodeCrypto
import com.juliusbaer.premarket.helpers.encryptor.DeCryptor
import com.juliusbaer.premarket.helpers.encryptor.EnCryptor
import com.juliusbaer.premarket.helpers.encryptor.SecurityController
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class StorageModule {
    @Provides
    @Singleton
    fun provideUserStorage(context: Context, securityController: SecurityController): IUserStorage = PreferenceUserStorage(context = context, securityController = securityController)

    @Provides
    @Singleton
    fun provideNodeCrypto(): NodeCrypto {
        return NodeCrypto()
    }

    @Provides
    @Singleton
    fun provideEnCryptor(): EnCryptor {
        return EnCryptor()
    }

    @Provides
    @Singleton
    fun provideDeCryptor(): DeCryptor {
        return DeCryptor()
    }

    @Provides
    @Singleton
    fun provideSecurityController(enCryptor: EnCryptor, deCryptor: DeCryptor, context: Context): SecurityController {
        return SecurityController(enCryptor, deCryptor, context)
    }
}