package com.juliusbaer.premarket.ui.markets.indices

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.dataFlow.NetworkStateManager
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.models.serverModels.IndexModel
import com.juliusbaer.premarket.ui.base.BaseNFragment
import com.juliusbaer.premarket.ui.base.HasOfflinePlaceHolder
import com.juliusbaer.premarket.ui.base.NavigationHost
import com.juliusbaer.premarket.ui.fragments.extentions.formatDate
import com.juliusbaer.premarket.ui.indexDetail.IndexDetailFragment
import com.juliusbaer.premarket.utils.Constants.DATE_FORMAT
import kotlinx.android.synthetic.main.fragment_markets_indices.*

class MarketsIndicesFragment : BaseNFragment(R.layout.fragment_markets_indices), HasOfflinePlaceHolder {
    private val viewModel by viewModels<MarketsIndicesViewModel> { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.setHasFixedSize(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.itemsLiveData.observe(viewLifecycleOwner, Observer {
            progressDialog.hide()
            when (it) {
                is Resource.Success -> {
                    populateUI(it.data)
                    viewModel.subscribeToRtUpdates(it.data.map { index -> index.id })
                }
                is Resource.Failure -> {
                    it.data?.let { data -> populateUI(data) }
                    if (!it.hasBeenHandled) parseError(it.e()!!)
                }
            }
        })
        viewModel.productModelLiveData.observe(viewLifecycleOwner, Observer { productModel ->
            if ((recyclerView.adapter as? MarketsIndicesAdapter)?.updateItem(productModel) == true) {
                viewModel.updateIndex(productModel)

                setLastUpdated(System.currentTimeMillis() / 1000)
            }
        })
    }

    private fun populateUI(indexes: List<IndexModel>) {
        val adapter = (recyclerView.adapter as? MarketsIndicesAdapter)
                ?: MarketsIndicesAdapter(object : MarketsIndicesAdapter.OnItemClick {
                    override fun onClick(index: IndexModel) {
                        (activity as? NavigationHost)?.openFragment(IndexDetailFragment.newInstance(index.id, false, index), null, true)
                    }
                }).apply {
                    recyclerView.adapter = this
                }
        adapter.submitList(indexes)

        val date = indexes.maxBy { it.date ?: 0 }?.date
        setLastUpdated(date)
    }

    private fun setLastUpdated(date: Long?) {
        txtLastUpdatedAt.text = date?.formatDate(" $DATE_FORMAT", "Last updated at:") ?: ""
    }

    override fun onResume() {
        super.onResume()

        doRequests(NetworkStateManager.isNetworkAvailable(requireContext()))
    }

    private fun doRequests(isOnline: Boolean) {
        if (isOnline) viewModel.resubscribeToRtUpdates()

        if (isOnline || isFirstStart) {
            progressDialog.show()
            viewModel.loadIndexes()
        }
    }

    override fun onOffline() {
        if (recyclerView.adapter?.itemCount == 0) {
            super.onOffline()
        }
    }

    override fun onOnline() {
        doRequests(true)
    }

    override fun onPause() {
        viewModel.unsubscribeFromRtUpdates()
        super.onPause()
    }
}


