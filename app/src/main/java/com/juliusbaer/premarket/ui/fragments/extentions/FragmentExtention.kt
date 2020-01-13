package com.juliusbaer.premarket.ui.fragments.extentions

import android.content.Intent
import androidx.annotation.AnimRes
import androidx.annotation.IdRes
import androidx.annotation.StyleRes
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.ui.base.NavigationHost
import kotlinx.android.synthetic.main.include_toolbar.*

fun Fragment.replaceChildFragmentSafely(fragment: Fragment,
                                   tag: String?,
                                   addToBackStack: Boolean = false,
                                   @IdRes containerViewId: Int,
                                   @StyleRes style: Int = 0,
                                   @AnimRes enterAnimation: Int = 0,
                                   @AnimRes exitAnimation: Int = 0,
                                   @AnimRes popEnterAnimation: Int = 0,
                                   @AnimRes popExitAnimation: Int = 0) {
    if (!addToBackStack) {
        childFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }
    childFragmentManager.commit {
        setTransitionStyle(style)
        setCustomAnimations(enterAnimation, exitAnimation, popEnterAnimation, popExitAnimation)
        replace(containerViewId, fragment, tag)
        if (addToBackStack) addToBackStack(tag)
    }
}

fun Fragment.initToolbar() {
    toolbar?.apply {
        if (requireFragmentManager().backStackEntryCount > 0) {
            (activity as? NavigationHost)?.let { host ->
                txtPageTitle.text = host.getBackTitle()
            }
            setNavigationIcon(R.drawable.icon_caret_left_white)
            setNavigationOnClickListener {
                activity?.onBackPressed()
            }
        }
    }
}

fun Fragment.makeCall(number: String): Boolean {
    return try {
        val intent = Intent(Intent.ACTION_CALL, "tel:$number".toUri())
        startActivity(intent)
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
