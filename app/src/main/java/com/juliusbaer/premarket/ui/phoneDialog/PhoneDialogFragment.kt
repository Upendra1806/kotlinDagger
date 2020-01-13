package com.juliusbaer.premarket.ui.phoneDialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.ui.ProgressDialog
import com.juliusbaer.premarket.ui.base.BaseNFragment
import com.juliusbaer.premarket.core.viewmodel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_phone_dialog.*
import timber.log.Timber
import javax.inject.Inject

class PhoneDialogFragment : DialogFragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by viewModels<PhoneDialogViewModel> { viewModelFactory }
    private val progressDialog by lazy { ProgressDialog.progressDialog(requireContext(), false) }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_phone_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        no.setOnClickListener { dismiss() }
        yes.setOnClickListener {
            val number = phone.text.toString().removePrefix("tel:")
            progressDialog.show()
            viewModel.savePhoneNumber(number)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.phoneUpdateLiveData.observe(viewLifecycleOwner, Observer {
            progressDialog.hide()
            when (it) {
                is Resource.Success -> {
                    val phoneNumber = it.data
                    val parent = (parentFragment as? BaseNFragment)
                    dismiss()
                    if (!phoneNumber.isBlank()) parent?.callPhone(it.data)
                }
                is Resource.Failure -> Timber.e(it.e())
            }
        })
    }

    override fun onDestroyView() {
        progressDialog.dismiss()
        super.onDestroyView()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        setStyle(STYLE_NO_FRAME, 0)
        isCancelable = false

        return Dialog(requireContext())
    }
}