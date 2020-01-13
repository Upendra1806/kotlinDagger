package com.juliusbaer.premarket.ui.filter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.fragment.app.Fragment
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.models.FilterModel
import com.juliusbaer.premarket.ui.base.BaseActivity
import com.juliusbaer.premarket.ui.base.NavigationChild
import com.juliusbaer.premarket.ui.base.NavigationHost
import com.juliusbaer.premarket.ui.fragments.extentions.replaceFragmentSafely

class FilterActivity : BaseActivity(R.layout.activity_filter), NavigationHost {
    companion object {
        private const val EXTRA_PRODUCT_ID = "PRODUCT_ID"
        private const val EXTRA_FILTER = "FILTER"

        fun newIntent(context: Context, underlyingId: Int? = null, filter: FilterModel? = null): Intent {
            val intent = Intent(context, FilterActivity::class.java)
            underlyingId?.let { intent.putExtra(EXTRA_PRODUCT_ID, it) }
            filter?.let { intent.putExtra(EXTRA_FILTER, it) }
            return intent
        }
    }

    private val backStackTitles = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            openFragment(FilterFragment.newInstance(
                    intent.getIntExtra(EXTRA_PRODUCT_ID, 0),
                    intent.getParcelableExtra(EXTRA_FILTER)), "", false)
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0 && backStackTitles.isNotEmpty()) {
            backStackTitles.removeAt(backStackTitles.size - 1)
        }
        super.onBackPressed()
    }

    override fun getBackTitle(): String {
        return if (backStackTitles.isNotEmpty()) backStackTitles[backStackTitles.size - 1] else getString(R.string.back)
    }

    override fun openFragment(fragment: Fragment, tag: String?, addToBackStack: Boolean, @StyleRes style: Int, enterAnimation: Int, exitAnimation: Int, popEnterAnimation: Int, popExitAnimation: Int) {
        if (addToBackStack) {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.containerLayout)
            backStackTitles.add(getString(if (currentFragment is NavigationChild) currentFragment.titleResId else R.string.back))
        } else {
            backStackTitles.clear()
        }
        replaceFragmentSafely(
                fragment,
                tag,
                addToBackStack,
                R.id.containerLayout,
                style,
                enterAnimation, exitAnimation, popEnterAnimation, popExitAnimation
        )
    }
}
