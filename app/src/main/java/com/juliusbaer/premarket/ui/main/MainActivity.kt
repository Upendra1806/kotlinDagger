package com.juliusbaer.premarket.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.ui.base.BaseNActivity
import com.juliusbaer.premarket.ui.base.NavigationHost
import com.juliusbaer.premarket.ui.disclaimer.DisclaimerActivity

class MainActivity : BaseNActivity(R.layout.activity_main), NavigationHost {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            val fragment = HomeFragment()
            supportFragmentManager.commit {
                    setPrimaryNavigationFragment(fragment)
                    setTransitionStyle(R.style.FragStyle)
                    setCustomAnimations(0, 0, 0, 0)
                    replace(R.id.fragment, fragment, HomeFragment.TAG)
            }
            if (storage.getFirstTimeLoading()) {
                startActivity(Intent(this, DisclaimerActivity::class.java))
            }
        }
    }

    override fun checkPromotion(): Boolean {
        return if (firstStart) {
            !storage.getFirstTimeLoading()
        } else {
            super.checkPromotion()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        (supportFragmentManager.findFragmentByTag(HomeFragment.TAG) as? HomeFragment)?.checkNotifications(intent?.extras)
    }

    override fun onBackPressed() {
        overridePendingTransition(R.anim.stay, R.anim.slide_down)
        super.onBackPressed()
    }

    override fun getBackTitle(): String {
        return (supportFragmentManager.findFragmentByTag(HomeFragment.TAG) as? NavigationHost)?.getBackTitle()
                ?: getString(R.string.back)
    }

    override fun openFragment(fragment: Fragment, tag: String?, addToBackStack: Boolean, @StyleRes style: Int, enterAnimation: Int, exitAnimation: Int, popEnterAnimation: Int, popExitAnimation: Int) {
        val navHostFragment = supportFragmentManager.findFragmentByTag(HomeFragment.TAG) as? NavigationHost
        navHostFragment?.openFragment(
                fragment,
                tag,
                addToBackStack,
                style,
                enterAnimation, exitAnimation, popEnterAnimation, popExitAnimation
        )
    }
}
