package com.juliusbaer.premarket.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.FragmentManager
import com.juliusbaer.premarket.dataFlow.IUserStorage
import com.juliusbaer.premarket.ui.dialogs.NoNetworkDialog
import com.juliusbaer.premarket.ui.dialogs.ServerErrorDialog
import com.juliusbaer.premarket.ui.fragments.extentions.toast
import com.juliusbaer.premarket.ui.login.LoginActivity
import retrofit2.HttpException
import java.net.HttpURLConnection
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException


/**
 * Contains common methods for UI elements.
 *
 * @author Q-DIS
 */
object UiUtils {

    /**
     * Check compatibility with api level = 26 (O)
     *
     * @return true, if current version >= 26
     */
    val isCompatWithO: Boolean
        get() = isCompatWith(Build.VERSION_CODES.O)

    /**
     * Check compatibility with api level = 23 (M)
     *
     * @return true, if current version >= 23
     */
    val isCompatWithM: Boolean
        get() = isCompatWith(Build.VERSION_CODES.M)

    /**
     * Check compatibility with api level = 24 (N)
     *
     * @return true, if current version >= 24
     */
    val isCompatWithN: Boolean
        get() = isCompatWith(Build.VERSION_CODES.N)

    /**
     * Create view
     *
     * @param context android context
     * @param layoutId used for creating new view
     */
    fun <T : View> inflate(context: Context, layoutId: Int): T {
        return LayoutInflater.from(context).inflate(layoutId, null) as T
    }

    /**
     * Create view
     *
     * @param layoutId used for creating new view
     * @param root created view will be added to root.
     */
    fun <T : View> inflate(root: ViewGroup, layoutId: Int): T {
        return LayoutInflater.from(root.context).inflate(layoutId, root, false) as T
    }

    /**
     * Check compatibility with api level = versionCode
     *
     * @param versionCode Android API level
     * @return true, if current version >= versionCode
     */
    private fun isCompatWith(versionCode: Int): Boolean {
        return Build.VERSION.SDK_INT >= versionCode
    }

    /**
     * Check is current device is tablet or not based on specified value for current screen width
     *
     * @param context app context
     * @return true if device is tablet, false otherwise
     */
//    fun isTablet(context: Context): Boolean {
//        return context.resources.getBoolean(R.bool.isTablet)
//    }

    /**
     * Hide soft keyboard. Do nothing if keyboard is not opened
     *
     * @param decorView View
     */
    fun hideKeyboard(decorView: View): Boolean {
        val inputManager = decorView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        return inputManager.hideSoftInputFromWindow(decorView.windowToken, 0)
    }

    /**
     * Hide soft keyboard. Do nothing if keyboard is not opened
     *
     * @param context [Context] app context
     * @param editText [EditText] to hide keyboard for
     */
    fun hideKeyboard(context: Context, editText: EditText) {
        hideKeyboard(context, editText.windowToken)
    }

    /**
     * Hide soft keyboard. Do nothing if keyboard is not opened
     *
     * @param context [Context] app context
     * @param windowToken [IBinder] window token to hide keyboard for
     */
    fun hideKeyboard(context: Context, windowToken: IBinder) {
        val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(windowToken, 0)
    }

    /**
     * Force open keyboard for given [EditText]
     *
     * @param context [Context] app context
     * @param editText [EditText] to show keyboard for
     */
    fun showKeyBoard(context: Context, editText: EditText) {
        val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.showSoftInput(editText, 0)
    }

    fun keyboardShown(rootView: View): Boolean {
        val r = Rect()
        rootView.getWindowVisibleDisplayFrame(r)
        val heightDiff = rootView.bottom - r.bottom
        return heightDiff > 100 * rootView.resources.displayMetrics.density
    }

    fun parseError(it: Throwable,
                   context: Activity,
                   fm: FragmentManager,
                   storage: IUserStorage,
                   forceAlerts: Boolean = false,
                   offlineCallback: () -> Unit) {
        when (it) {
            is SocketTimeoutException,
            is SocketException,
            is UnknownHostException -> {
                if (forceAlerts) {
                    NoNetworkDialog().show(fm, NoNetworkDialog.TAG)
                } else {
                    if (!storage.noInternetAlertShown) {
                        storage.noInternetAlertShown = true

                        if (fm.findFragmentByTag(NoNetworkDialog.TAG) == null) {
                            offlineCallback()
                            NoNetworkDialog().show(fm, NoNetworkDialog.TAG)
                        }
                    } else {
                        offlineCallback()
                    }
                }
            }
            is HttpException -> {
                when ((it).code()) {
                    HttpURLConnection.HTTP_BAD_REQUEST -> context.toast("Error: ${it.message()}")
                    HttpURLConnection.HTTP_UNAUTHORIZED -> if (context !is LoginActivity) {
                        context.startActivity(Intent(context, LoginActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                    } else {
                        showServerError(fm)
                    }
                    HttpURLConnection.HTTP_FORBIDDEN -> if (context !is LoginActivity) {
                        context.startActivity(Intent(context, LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                    } else {
                        showServerError(fm)
                    }
                    HttpURLConnection.HTTP_NOT_FOUND -> showServerError(fm)
                    HttpURLConnection.HTTP_INTERNAL_ERROR -> showServerError(fm)
                    else -> showServerError(fm)
                }
            }
        }
    }

    private fun showServerError(fm :FragmentManager) {
        if (fm.findFragmentByTag(ServerErrorDialog.TAG) == null) {
            ServerErrorDialog().show(fm, ServerErrorDialog.TAG)
        }
    }
}
