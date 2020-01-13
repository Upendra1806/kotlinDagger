package com.juliusbaer.premarket.ui.customViews

import android.content.Context
import android.util.AttributeSet
import android.widget.AutoCompleteTextView

class AutoCompleteTextViewCustom(context: Context?, attrs: AttributeSet?) : AutoCompleteTextView(context, attrs) {
    private var filterCompleteListener: ((count: Int) -> Unit)? = null

    override fun onFilterComplete(count: Int) {
        super.onFilterComplete(count)
        filterCompleteListener?.invoke(count)
    }

    fun setFilterCompleteListener(listener: ((count: Int) -> Unit)?) {
        filterCompleteListener = listener
    }
}