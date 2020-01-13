package com.juliusbaer.premarket.ui.base

import androidx.annotation.AnimRes
import androidx.annotation.StyleRes
import androidx.fragment.app.Fragment

interface NavigationHost {
    fun getBackTitle(): String
    fun openFragment(fragment: Fragment,
                     tag: String?,
                     addToBackStack: Boolean = false,
                     @StyleRes style: Int = 0,
                     @AnimRes enterAnimation: Int = 0,
                     @AnimRes exitAnimation: Int = 0,
                     @AnimRes popEnterAnimation: Int = 0,
                     @AnimRes popExitAnimation: Int = 0)
}