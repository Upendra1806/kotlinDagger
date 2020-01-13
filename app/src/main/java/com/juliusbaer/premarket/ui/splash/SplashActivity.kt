package com.juliusbaer.premarket.ui.splash

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.dataFlow.IUserStorage
import com.juliusbaer.premarket.helpers.HardwareIdProvider
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.ui.fragments.extentions.createDefaultConfiguration
import com.juliusbaer.premarket.ui.main.MainActivity
import com.juliusbaer.premarket.core.viewmodel.ViewModelFactory
import dagger.android.AndroidInjection
import retrofit2.HttpException
import timber.log.Timber
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

class SplashActivity : AppCompatActivity(R.layout.activity_splash) {
    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var storage: IUserStorage

    private val viewModel by viewModels<SplashViewModel> { viewModelFactory }

    private var invalidInstallationIdProcessed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)

        super.onCreate(savedInstanceState)

        if (storage.getFirstTimeLoading()) {
            viewModel.setUserId(HardwareIdProvider(this).deviceId)
        }
        viewModel.authLiveData.observe(this, Observer {
            when (it) {
                is Resource.Success -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                is Resource.Failure -> if (!it.hasBeenHandled) {
                    val throwable = it.e()!!
                    Timber.e(throwable)

                    //resets userId in case of error request, at 30.04.2019 it's 400 Bad Request {"error":"invalid_grant","error_description":"Invalid installationId"}
                    if (!invalidInstallationIdProcessed && throwable is HttpException && throwable.code() >= 300 && throwable.code() < 500) {
                        viewModel.setUserId(HardwareIdProvider(this).deviceId)
                        invalidInstallationIdProcessed = true
                        viewModel.auth()
                    } else {
                        when (throwable) {
                            is SocketTimeoutException,
                            is SocketException,
                            is UnknownHostException -> {
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                            else -> finish()
                        }
                    }
                }
            }
        })
        if (savedInstanceState == null) viewModel.auth()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase.createDefaultConfiguration())
    }
}
