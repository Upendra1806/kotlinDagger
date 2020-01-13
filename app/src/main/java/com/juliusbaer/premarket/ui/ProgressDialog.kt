package com.juliusbaer.premarket.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import com.juliusbaer.premarket.R

class ProgressDialog {
    companion object {
        fun progressDialog(context: Context, isCancellable: Boolean = false): Dialog {
            val dialog = Dialog(context)
            val inflate = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null)
            dialog.setContentView(inflate)
            dialog.setCancelable(isCancellable)
            dialog.window!!.setBackgroundDrawable(
                    ColorDrawable(Color.TRANSPARENT))
            return dialog
        }
    }
}