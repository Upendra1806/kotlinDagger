package com.juliusbaer.premarket.notification

import com.google.firebase.iid.FirebaseInstanceIdService
import com.juliusbaer.premarket.dataFlow.AuthRepository
import com.juliusbaer.premarket.dataFlow.IUserStorage
import dagger.android.AndroidInjection
import kotlinx.coroutines.runBlocking
import timber.log.Timber

class MyFirebaseInstanceIdService : FirebaseInstanceIdService() {
    var authRepository: AuthRepository? = null
    var storage: IUserStorage? = null

    override fun onTokenRefresh() {
        if (authRepository == null) {
            AndroidInjection.inject(this)
        }
        runBlocking {
            try {
                authRepository?.registration(storage?.getEmail())
            } catch (e: Throwable) {
                Timber.e(e)
            }
        }
    }
}