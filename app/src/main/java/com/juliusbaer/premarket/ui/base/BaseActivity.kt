package com.juliusbaer.premarket.ui.base


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.SystemClock
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.viewModels
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.core.viewmodel.ViewModelFactory
import com.juliusbaer.premarket.dataFlow.IUserStorage
import com.juliusbaer.premarket.dataFlow.NetworkStateManager
import com.juliusbaer.premarket.di.RealAppProvider
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.ui.OfflineFragment
import com.juliusbaer.premarket.ui.fragments.extentions.createDefaultConfiguration
import com.juliusbaer.premarket.ui.fragments.extentions.replaceFragmentSafely
import com.juliusbaer.premarket.ui.promotion.PromotionActivity
import com.juliusbaer.premarket.ui.promotion.PromotionViewModel
import com.juliusbaer.premarket.utils.Constants
import com.juliusbaer.premarket.utils.UiUtils
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import timber.log.Timber
import javax.inject.Inject

abstract class BaseActivity(@LayoutRes layoutId: Int) : AppCompatActivity(layoutId), HasAndroidInjector {
    private var lastClickTime = SystemClock.elapsedRealtime()

    @Inject
    lateinit var storage: IUserStorage

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var networkStateManager: NetworkStateManager

    private val promotionViewModel by viewModels<PromotionViewModel> { viewModelFactory  }

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector(): AndroidInjector<Any> {
        return androidInjector
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)

        super.onCreate(savedInstanceState)

        networkStateManager.networkStateLiveData.observe(this, Observer { isOnline ->
            if (isOnline) {
                supportFragmentManager.findFragmentByTag(Constants.OFFLINE_FRAGMENT)?.let { fragment ->
                    supportFragmentManager.commit {
                        remove(fragment)
                    }
                }
                onOnline()
            }
        })
        promotionViewModel.promotionLiveData.observe(this, Observer {
            if (it is Resource.Success) {
                it.data?.let { promotionInfo ->
                    PromotionActivity.promotionModel = promotionInfo
                    startActivity(Intent(this, PromotionActivity::class.java))
                }
            }
        })
    }

    protected open fun onOffline() {
        replaceFragmentSafely(OfflineFragment(), Constants.OFFLINE_FRAGMENT, addToBackStack = false, containerViewId = R.id.contentHolder)
    }

    protected open fun onOnline() {
    }

    fun parseError(error: Throwable, forceAlerts: Boolean = false) {
        Timber.e(error)
        UiUtils.parseError(error, this, supportFragmentManager, storage, forceAlerts) { setOffline() }
    }

    private fun setOffline() {
        networkStateManager.setOffline()
        if (supportFragmentManager.findFragmentByTag(Constants.OFFLINE_FRAGMENT) == null) {
            onOffline()
        }
    }

    protected fun isClickAllowed(): Boolean {
        val now = SystemClock.elapsedRealtime()
        return if (now - lastClickTime > Constants.CLICK_TIME_INTERVAL) {
            lastClickTime = now
            true
        } else
            false
    }

    fun hideSoftKeyboard() {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    hideSoftKeyboard()
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onResume() {
        super.onResume()

        if (storage.promotionLastModifiedDate == 0L || (applicationContext as? RealAppProvider)?.wasInBackground == true) {
            promotionViewModel.getPromotion()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase.createDefaultConfiguration())
    }
}




