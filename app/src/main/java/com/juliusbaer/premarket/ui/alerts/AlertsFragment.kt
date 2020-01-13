package com.juliusbaer.premarket.ui.alerts


import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.dataFlow.NetworkStateManager
import com.juliusbaer.premarket.models.ProductType
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.models.serverModels.AlertsModel
import com.juliusbaer.premarket.ui.base.BaseNFragment
import com.juliusbaer.premarket.ui.base.HasOfflinePlaceHolder
import com.juliusbaer.premarket.ui.fragments.extentions.initToolbar
import kotlinx.android.synthetic.main.fragment_alerts.*
import kotlinx.android.synthetic.main.header_app.*

class AlertsFragment : BaseNFragment(R.layout.fragment_alerts), HasOfflinePlaceHolder {
    private val viewModel by viewModels<AlertsVM> { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        deleteAll.setOnClickListener {
            DeleteAllDialog().show(childFragmentManager, "deleteAll")
        }
        initToolbar()
        screenTitle.setText(R.string.alerts)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.itemsLiveData.observe(viewLifecycleOwner, Observer {
            progressDialog.hide()
            when (it) {
                is Resource.Success -> setData(it.data)
                is Resource.Failure -> {
                    it.data?.let { data -> setData(data) }
                    if (!it.hasBeenHandled) parseError(it.e()!!)
                }
            }
        })
        viewModel.alertDelAll.observe(viewLifecycleOwner, Observer {
            progressDialog.hide()
            when (it) {
                is Resource.Success -> viewModel.loadAlerts()
                is Resource.Failure -> if (!it.hasBeenHandled) parseError(it.e()!!, true)
            }
        })
        viewModel.alertDel.observe(viewLifecycleOwner, Observer {
            progressDialog.hide()
            when (it) {
                is Resource.Success -> viewModel.loadAlerts()
                is Resource.Failure -> if (!it.hasBeenHandled) parseError(it.e()!!, true)
            }
        })
    }

    private fun setData(items: List<AlertsModel>) {
        val adapter = (recycleView.adapter as? AlertsAdapter)?: AlertsAdapter(object : AlertsAdapter.AlertsClickListener {
            override fun onDeleteAlert(productId: Int) {
                DeleteAlertDialog.newInstance(productId).show(childFragmentManager, "deleteAlert")
            }

            override fun onItemClick(item: AlertsModel) {
                ProductType.values().firstOrNull { it.v == item.productType }?.let {
                    startActivity(UnderlyingAlertsActivity.newIntent(requireContext(), item.productId, it, item.precision))
                }
            }
        }).apply {
            recycleView.adapter = this
        }
        adapter.submitList(items)

        val hasItems = items.isNotEmpty()
        recycleView.isVisible = hasItems
        imgLast.isVisible = hasItems
        txtManageAlerts.isVisible = hasItems
        deleteAll.isVisible = hasItems

        noResult.isVisible = !hasItems
    }

    override fun onResume() {
        super.onResume()

        doRequests(NetworkStateManager.isNetworkAvailable(requireContext()))
    }

    private fun doRequests(isOnline: Boolean) {
        if (isOnline || isFirstStart) {
            progressDialog.show()
            viewModel.loadAlerts()
        }
    }

    override fun onOffline() {
        if (recycleView.adapter?.itemCount == 0) {
            super.onOffline()
        }
    }

    override fun onOnline() {
        doRequests(true)
    }

    private fun deleteAllAlerts() {
        progressDialog.show()
        viewModel.alertDeleteAll()
    }

    private fun deleteAlert(id: Int) {
        progressDialog.show()
        viewModel.alertDeleteRequest(id)
    }

    class DeleteAlertDialog : DialogFragment() {
        companion object {
            private const val ARG_PRODUCT_ID = "productId"

            fun newInstance(id: Int): DeleteAlertDialog {
                return DeleteAlertDialog().apply {
                    arguments = bundleOf(ARG_PRODUCT_ID to id)
                }
            }
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return AlertDialog.Builder(requireContext())
                    .setMessage(R.string.are_you_sure_dont_want_receive_alerts)
                    .setPositiveButton(R.string.delete) { _, _ ->
                        (parentFragment as? AlertsFragment)?.deleteAlert(requireArguments().getInt(ARG_PRODUCT_ID))
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
        }
    }

    class DeleteAllDialog : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return AlertDialog.Builder(requireContext())
                    .setMessage(R.string.are_you_sure_unset_all_alerts)
                    .setPositiveButton(R.string.delete) { _, _ ->
                        (parentFragment as? AlertsFragment)?.deleteAllAlerts()
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
        }
    }
}

