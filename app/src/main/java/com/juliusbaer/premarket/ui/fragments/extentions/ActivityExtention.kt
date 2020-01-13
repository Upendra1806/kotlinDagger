package com.juliusbaer.premarket.ui.fragments.extentions

import android.content.Context
import android.widget.Toast
import androidx.annotation.AnimRes
import androidx.annotation.IdRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import java.util.*

/**
 * Method to replace the fragment. The [fragment] is added to the container view with id
 * [containerViewId] and a [tag]. The operation is performed by the supportFragmentManager.
 */
fun AppCompatActivity.replaceFragmentSafely(fragment: Fragment,
                                            tag: String?,
                                            addToBackStack: Boolean = false,
                                            @IdRes containerViewId: Int,
                                            @StyleRes style: Int = 0,
                                            @AnimRes enterAnimation: Int = 0,
                                            @AnimRes exitAnimation: Int = 0,
                                            @AnimRes popEnterAnimation: Int = 0,
                                            @AnimRes popExitAnimation: Int = 0) {
    supportFragmentManager.commit {
        setTransitionStyle(style)
        setCustomAnimations(enterAnimation, exitAnimation, popEnterAnimation, popExitAnimation)
        replace(containerViewId, fragment, tag)
        if (addToBackStack) addToBackStack(tag)
    }
}

/**
 * Method to add the fragment. The [fragment] is added to the container view with id
 * [containerViewId] and a [tag]. The operation is performed by the supportFragmentManager.
 * This method checks if fragment exists.
 * @return the fragment added.
 */
fun <T : Fragment> AppCompatActivity.addFragmentSafely(fragment: T,
                                                       tag: String,
                                                       addToBackStack: Boolean = false,
                                                       @IdRes containerViewId: Int,
                                                       @StyleRes style: Int = 0,
                                                       @AnimRes enterAnimation: Int = 0,
                                                       @AnimRes exitAnimation: Int = 0,
                                                       @AnimRes popEnterAnimation: Int = 0,
                                                       @AnimRes popExitAnimation: Int = 0): T {
    if (!existsFragmentByTag(tag)) {
        supportFragmentManager.commit {
            setTransitionStyle(style)
            setCustomAnimations(enterAnimation, exitAnimation, popEnterAnimation, popExitAnimation)
            add(containerViewId, fragment, tag)
            if (addToBackStack) addToBackStack(tag)
        }
        return fragment
    }
    return findFragmentByTag(tag) as T
}

fun Context.toast(resourceId: Int) = Toast.makeText(this, resourceId, Toast.LENGTH_SHORT).show()
fun Context.toast(message: CharSequence) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun Context.createDefaultConfiguration(): Context {
    val configuration = resources.configuration.apply {
        fontScale = 1f
        setLocale(Locale("en"))
    }
    return createConfigurationContext(configuration)
}


/**
 * Method to check if fragment exists. The operation is performed by the supportFragmentManager.
 */
fun AppCompatActivity.existsFragmentByTag(tag: String): Boolean =
        supportFragmentManager.findFragmentByTag(tag) != null

/**
 * Method to get fragment by tag. The operation is performed by the supportFragmentManager.
 */
fun AppCompatActivity.findFragmentByTag(tag: String): Fragment? =
        supportFragmentManager.findFragmentByTag(tag)


