package com.juliusbaer.premarket.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.AnimRes
import androidx.annotation.StyleRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.models.ProductType
import com.juliusbaer.premarket.models.serverModels.FxType
import com.juliusbaer.premarket.ui.base.BaseNFragment
import com.juliusbaer.premarket.ui.base.NavigationChild
import com.juliusbaer.premarket.ui.base.NavigationHost
import com.juliusbaer.premarket.ui.company.CompanyFragment
import com.juliusbaer.premarket.ui.detailNews.NewsDetailFragment
import com.juliusbaer.premarket.ui.detailWarrant.WarrantDetailFragment
import com.juliusbaer.premarket.ui.fragments.extentions.replaceChildFragmentSafely
import com.juliusbaer.premarket.ui.indexDetail.IndexDetailFragment
import com.juliusbaer.premarket.ui.login.LoginActivity
import com.juliusbaer.premarket.ui.markets.MarketsFragment
import com.juliusbaer.premarket.ui.markets.fx.FxDetailFragment
import com.juliusbaer.premarket.ui.more.MoreFragment
import com.juliusbaer.premarket.ui.news.NewsFragment
import com.juliusbaer.premarket.ui.underluings.UnderlyingsFragment
import com.juliusbaer.premarket.ui.watchlist.WatchlistFragment
import kotlinx.android.synthetic.main.fragment_home_market.*
import me.leolin.shortcutbadger.ShortcutBadger
import timber.log.Timber

class HomeFragment : BaseNFragment(R.layout.fragment_home_market), NavigationHost {
    companion object {
        const val TAG = "home"
    }

    private val viewModel by viewModels<HomeViewModel> { viewModelFactory }

    private val backStackTitles = mutableListOf<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabBarHome.itemIconTintList = null
        tabBarHome.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.equities -> openFragment(UnderlyingsFragment(), UnderlyingsFragment.TAG, false)
                R.id.markets -> openFragment(MarketsFragment(), MarketsFragment.TAG, false)
                R.id.watchlist -> {
                    if (!viewModel.isConfirmed()) {
                        context?.startActivity(Intent(context, LoginActivity::class.java))
                    } else {
                        openFragment(WatchlistFragment(), WatchlistFragment.TAG, false)
                    }
                }
                R.id.news -> openFragment(NewsFragment(), NewsFragment.TAG, false)
                R.id.more -> openFragment(MoreFragment(), MoreFragment.TAG, false)
            }
            true
        }
        if (savedInstanceState == null) {
            openFragment(UnderlyingsFragment(), UnderlyingsFragment.TAG, false)

            checkNotifications(activity?.intent?.extras)
        }
    }

    fun checkNotifications(bundle: Bundle?) {
        if (bundle != null) {
            val counter = storage.badgeCount
            if (counter > 0) {
                storage.badgeCount = counter - 1
            }
            ShortcutBadger.applyCount(context, counter - 1)

            val productId = bundle.getString("ProductId")?.toInt() ?: return
            val productTypeInt = bundle.getString("ProductTypeId")?.toInt() ?: return
            val productType = ProductType.values().firstOrNull { it.v == productTypeInt }
            when (productType) {
                ProductType.NEWS -> NewsDetailFragment.newInstance(productId, true)
                ProductType.INDEX -> IndexDetailFragment.newInstance(productId, true)
                ProductType.UNDERLYING -> CompanyFragment.newInstance(productId, true)
                ProductType.WARRANT -> WarrantDetailFragment.newInstance(productId, true)
                ProductType.CURRENCY -> FxDetailFragment.newInstance(productId, FxType.CURRENCY, true)
                ProductType.METAL -> FxDetailFragment.newInstance(productId, FxType.METAL, true)
                else -> null
            }?.let {
                openFragment(it as Fragment, null, true)
            }
            Timber.d("Notification productType = $productType; productId = $productId ")
        }
    }

    override fun getBackTitle(): String {
        return if (backStackTitles.isNotEmpty()) backStackTitles[backStackTitles.size - 1] else getString(R.string.back)
    }

    fun onBackPressed() {
        if (childFragmentManager.backStackEntryCount > 0 && backStackTitles.isNotEmpty()) {
            backStackTitles.removeAt(backStackTitles.size - 1)
        }
    }

    override fun openFragment(fragment: Fragment,
                              tag: String?,
                              addToBackStack: Boolean,
                              @StyleRes style: Int,
                              @AnimRes enterAnimation: Int,
                              @AnimRes exitAnimation: Int,
                              @AnimRes popEnterAnimation: Int,
                              @AnimRes popExitAnimation: Int) {

        if (addToBackStack) {
            val currentFragment = childFragmentManager.findFragmentById(R.id.containerLayout)
            backStackTitles.add(getString(if (currentFragment is NavigationChild) currentFragment.titleResId else R.string.back))
        } else {
            backStackTitles.clear()
        }
        if (!addToBackStack && tag != null) {
            childFragmentManager.findFragmentByTag(tag)?.let {
                childFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                return
            }
        }
        replaceChildFragmentSafely(
                fragment,
                tag,
                addToBackStack,
                R.id.containerLayout,
                style,
                enterAnimation, exitAnimation, popEnterAnimation, popExitAnimation)
    }
}



