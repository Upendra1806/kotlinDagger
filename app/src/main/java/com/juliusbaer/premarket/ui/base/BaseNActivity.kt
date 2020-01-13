package com.juliusbaer.premarket.ui.base

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.juliusbaer.premarket.core.viewmodel.ViewModelFactory
import com.juliusbaer.premarket.dataFlow.IUserStorage
import com.juliusbaer.premarket.di.RealAppProvider
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.ui.ProgressDialog
import com.juliusbaer.premarket.ui.fragments.extentions.createDefaultConfiguration
import com.juliusbaer.premarket.ui.promotion.PromotionActivity
import com.juliusbaer.premarket.ui.promotion.PromotionViewModel
import com.juliusbaer.premarket.utils.UiUtils
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import timber.log.Timber
import javax.inject.Inject


abstract class BaseNActivity(@LayoutRes layoutId: Int) : AppCompatActivity(layoutId), HasAndroidInjector {
    @Inject lateinit var viewModelFactory: ViewModelFactory

    @Inject lateinit var storage: IUserStorage

    private val promotionViewModel by viewModels<PromotionViewModel> { viewModelFactory }

    protected var firstStart: Boolean = false
        private set

    protected val progressDialog by lazy { ProgressDialog.progressDialog(this, true) }

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector(): AndroidInjector<Any> {
        return androidInjector
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)

        super.onCreate(savedInstanceState)

        firstStart = savedInstanceState == null

        promotionViewModel.promotionLiveData.observe(this, Observer {
            if (it is Resource.Success) {
                it.data?.let { promotionInfo ->
                    PromotionActivity.promotionModel = promotionInfo
                    startActivity(Intent(this, PromotionActivity::class.java))
                }
            }
        })
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase.createDefaultConfiguration())
    }

    fun parseError(it: Throwable, forceAlerts: Boolean = false) {
        Timber.e(it)
        UiUtils.parseError(it, this, supportFragmentManager, storage, forceAlerts) {}
    }

    override fun onPause() {
        firstStart = false
        super.onPause()
    }

    override fun onResume() {
        super.onResume()

        if (checkPromotion()) promotionViewModel.getPromotion()
    }

    protected open fun checkPromotion(): Boolean {
        return storage.promotionLastModifiedDate == 0L || (applicationContext as? RealAppProvider)?.wasInBackground == true
    }
}