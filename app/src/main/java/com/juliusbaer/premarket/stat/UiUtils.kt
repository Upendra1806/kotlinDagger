package com.juliusbaer.premarket.stat

import android.content.Context
import com.juliusbaer.premarket.R

object UiUtils {
    /**
     * Check is current device is tablet or not based on specified value for current screen width
     *
     * @param context app context
     * @return true if device is tablet, false otherwise
     */
    fun isTablet(context: Context): Boolean {
        return context.resources.getBoolean(R.bool.isTablet)
    }
}
