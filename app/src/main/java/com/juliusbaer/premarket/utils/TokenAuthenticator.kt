package com.juliusbaer.premarket.utils

import com.juliusbaer.premarket.dataFlow.AuthRepository
import com.juliusbaer.premarket.dataFlow.IUserStorage
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import timber.log.Timber

class TokenAuthenticator(private val authRepository: AuthRepository, private val storage: IUserStorage) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        return if (response.request.header("Authorization") != null) {
            runBlocking {
                try {
                    authRepository.registration(storage.getEmail())
                } catch (e: Throwable) {
                    Timber.e(e)
                    null
                }
            }?.let {
                response.request
                        .newBuilder()
                        .header("Authorization", "Bearer ${it.accessToken}")
                        .build()
            }
        } else {
            null
        }
    }
}
