package com.juliusbaer.premarket.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.juliusbaer.premarket.BuildConfig
import com.juliusbaer.premarket.app.di.InterceptorsModule
import com.juliusbaer.premarket.dataFlow.AuthRepository
import com.juliusbaer.premarket.dataFlow.AuthRepositoryImpl
import com.juliusbaer.premarket.dataFlow.IUserStorage
import com.juliusbaer.premarket.dataFlow.network.Api
import com.juliusbaer.premarket.dataFlow.network.AuthApi
import com.juliusbaer.premarket.core.utils.DeferredCallAdapterFactory
import com.juliusbaer.premarket.utils.TokenAuthenticator
import dagger.Module
import dagger.Provides
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

/**
 * OkHttp, Retrofit dependencies split is used for TokenAuthenticator to avoid cycle dependency
 */
@Module(includes = [StorageModule::class, InterceptorsModule::class])
class NetworkModule {
    @Singleton
    @Provides
    fun provideHttpUrl(): HttpUrl = BuildConfig.API_BASE_URL.toHttpUrl()

    @Named("okhttpAuth")
    @Singleton
    @Provides
    fun provideOkHttpClientAuth(loggerInterceptor: HttpLoggingInterceptor,
                                storage: IUserStorage,
                                networkInterceptors: Set<@JvmSuppressWildcards Interceptor>): OkHttpClient {
        return makeOkHttpBuilder(loggerInterceptor, storage, networkInterceptors)
                .build()
    }

    @Named("retrofitAuth")
    @Singleton
    @Provides
    fun provideRetrofitAuth(httpUrl: HttpUrl,
                            @Named("okhttpAuth") client: OkHttpClient,
                            gson: Gson): Retrofit {
        return Retrofit.Builder()
                .baseUrl(httpUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
    }

    @Singleton
    @Provides
    fun provideAuthRepository(@Named("retrofitAuth") retrofit: Retrofit, storage: IUserStorage): AuthRepository {
        return AuthRepositoryImpl(retrofit.create(AuthApi::class.java), storage)
    }

    @Singleton
    @Provides
    fun provideHttpLoggerInterceptor(): HttpLoggingInterceptor {
        val logger = HttpLoggingInterceptor(object: HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                Timber.tag(NETWORK_TAG).d(message)
            }
        })
        logger.level = HttpLoggingInterceptor.Level.BODY

        return logger
    }

    @Named("okhttp")
    @Singleton
    @Provides
    fun provideOkHttpClient(loggerInterceptor: HttpLoggingInterceptor,
                            storage: IUserStorage,
                            authRepository: AuthRepository,
                            networkInterceptors: Set<@JvmSuppressWildcards Interceptor>): OkHttpClient =
            makeOkHttpBuilder(loggerInterceptor, storage, networkInterceptors)
                    .authenticator(TokenAuthenticator(authRepository, storage))
                    .build()

    @Named("retrofit")
    @Singleton
    @Provides
    fun provideRetrofit(httpUrl: HttpUrl,
                        @Named("okhttp") client: OkHttpClient,
                        gson: Gson): Retrofit {
        return Retrofit.Builder()
                .baseUrl(httpUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(DeferredCallAdapterFactory())
                .build()
    }

    @Singleton
    @Provides
    fun provideApiInterface(@Named("retrofit") retrofit: Retrofit): Api = retrofit.create(Api::class.java)

    @Singleton
    @Provides
    fun provideGson(): Gson = GsonBuilder()
            .serializeNulls()
            .create()

    companion object {
        private const val NETWORK_TAG = "API_NETWORK_PROVIDER"

        private fun makeOkHttpBuilder(loggerInterceptor: HttpLoggingInterceptor,
                                      storage: IUserStorage,
                                      networkInterceptors: Set<@JvmSuppressWildcards Interceptor>): OkHttpClient.Builder {
            val builder = OkHttpClient.Builder()
                    .addInterceptor(object: Interceptor {
                        override fun intercept(chain: Interceptor.Chain): Response {
                            val original = chain.request()
                            val request = original.newBuilder()
                                    .header("Authorization", "Bearer ${storage.getToken()}")
                                    .method(original.method, original.body)
                                    .build()
                            Timber.d("Token module:${storage.getToken()}")
                            return chain.proceed(request)
                        }
                    })
                    .addInterceptor(loggerInterceptor)
                    .connectTimeout(40, TimeUnit.SECONDS)
                    .readTimeout(40, TimeUnit.SECONDS)
                    .writeTimeout(40, TimeUnit.SECONDS)
            for (networkInterceptor in networkInterceptors) {
                builder.addNetworkInterceptor(networkInterceptor)
            }
            return builder
        }
    }
}