package com.juliusbaer.premarket.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.juliusbaer.premarket.R

import kotlinx.android.synthetic.main.no_network_dialog.*


class NoNetworkDialog: DialogFragment() {
    companion object {
        const val TAG = "NoNetworkDialog"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.no_network_dialog)
        dialog.no.setOnClickListener {
        }
        dialog.yes2.setOnClickListener {
            dismiss()
            requireFragmentManager().executePendingTransactions()
        }
        return dialog
    }
}