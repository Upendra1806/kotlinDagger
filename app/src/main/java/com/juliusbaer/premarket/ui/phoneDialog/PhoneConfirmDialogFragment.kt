package com.juliusbaer.premarket.ui.phoneDialog

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.ui.fragments.extentions.makeCall
import kotlinx.android.synthetic.main.fragment_phone_confirm_dialog.*

class PhoneConfirmDialogFragment : DialogFragment() {
    companion object {
        private const val ARG_PHONE = "phone"
        private const val REQUEST_PERM_CALL = 1

        fun newInstance(phone: String): PhoneConfirmDialogFragment {
            return PhoneConfirmDialogFragment().apply {
                arguments = bundleOf(ARG_PHONE to phone)
            }
        }
    }

    private val number by lazy {
        requireArguments().getString(ARG_PHONE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_phone_confirm_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title.text = number
        no.setOnClickListener { dismiss() }
        yes.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(),
                            Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.CALL_PHONE), REQUEST_PERM_CALL)
            } else {
                dismiss()
                makeCall(number)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        setStyle(STYLE_NO_FRAME, 0)
        isCancelable = false

        return Dialog(requireContext())
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERM_CALL) {
            try {
                if (grantResults.isNotEmpty()) {
                    dismiss()
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        makeCall(number)
                    }
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }
}