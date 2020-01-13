package com.juliusbaer.premarket.ui.markets.fx

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.tabs.TabLayout
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.dataFlow.NetworkStateManager
import com.juliusbaer.premarket.dataFlow.zeroMQ.SubscriptionPrefix
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.models.serverModels.FxBaseCurrency
import com.juliusbaer.premarket.models.serverModels.FxModel
import com.juliusbaer.premarket.models.serverModels.FxType
import com.juliusbaer.premarket.ui.base.BaseNFragment
import com.juliusbaer.premarket.ui.base.HasOfflinePlaceHolder
import com.juliusbaer.premarket.ui.base.NavigationHost
import com.juliusbaer.premarket.utils.Constants
import kotlinx.android.synthetic.main.fragment_markets_fx.*
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

class FxFragment : BaseNFragment(R.layout.fragment_markets_fx), HasOfflinePlaceHolder {
    private var tabPosition: Int = 0

    private val viewModel by viewModels<FxViewModel> { viewModelFactory }

    private val type by lazy {
        requireArguments().getSerializable(ARG_TYPE) as FxType
    }

    private val lastUpdatedFmt by lazy {
        DateTimeFormatter.ofPattern(getString(R.string.fmt_fx_last_updated_date), Locale.ENGLISH)
    }

    companion object {
        private const val ARG_TYPE = "type"
        private const val STATE_TAB = "tab"

        fun newInstance(type: FxType): FxFragment {
            return FxFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_TYPE, type)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tabPosition = savedInstanceState?.getInt(STATE_TAB) ?: 0
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        recyclerView.itemAnimator = null
        recyclerView.setHasFixedSize(true)

        val values = FxBaseCurrency.values()
        for (i in values.indices) {
            tabLayout.addTab(tabLayout.newTab().setText(values[i].titleResId), i == tabPosition)
        }
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                tabPosition = tab.position
                viewModel.loadFxList(type, FxBaseCurrency.values()[tab.position], true)
            }
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.clearTopics()
        viewModel.itemsLiveData.observe(viewLifecycleOwner, Observer {
            progressDialog.hide()
            when (it) {
                is Resource.Success -> {
                    val (items, scrollToTop) = it.data
                    populateUI(items, scrollToTop, true)
                }
                is Resource.Failure -> {
                    it.data?.let { (items, scrollToTop) -> populateUI(items, scrollToTop, false) }
                    if (!it.hasBeenHandled) parseError(it.e()!!)
                }
            }
        })
        viewModel.productModelLiveData.observe(viewLifecycleOwner, Observer { productModel ->
            if ((recyclerView.adapter as? FxAdapter)?.updateItem(productModel) == true) {
                setLastUpdated(0)

                viewModel.updateFxItem(productModel)
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_TAB, tabPosition)
    }


    private fun setLastUpdated(priceDateTime: Long) {
        val seconds = if (priceDateTime > 0) priceDateTime else System.currentTimeMillis() / 1000
        val formattedDate = LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds), Constants.swissZoneId).format(lastUpdatedFmt)
        txtLastUpdatedAt.text = getString(R.string.fmt_fx_last_updated, formattedDate)
    }

    private fun populateUI(items: List<FxModel>, scrollToTop: Boolean, subscribeToRtUpdates: Boolean) {
        val adapter = (recyclerView.adapter as? FxAdapter) ?: FxAdapter { item ->
            (activity as? NavigationHost)?.openFragment(FxDetailFragment.newInstance(item.id, type, false, item), null, true)
        }.apply {
            recyclerView.adapter = this
        }
        adapter.submitList(items)

        if (scrollToTop) recyclerView.scrollToPosition(0)

        setLastUpdated(items.maxBy { it.priceDateTime ?: 0 }?.priceDateTime ?: 0)

        if (subscribeToRtUpdates) viewModel.subscribeToRtUpdates(items.map { it.id }, SubscriptionPrefix.PRODUCT)

    }

    override fun onResume() {
        super.onResume()

        doRequests(NetworkStateManager.isNetworkAvailable(requireContext()))
    }

    override fun onPause() {
        viewModel.unsubscribeFromRtUpdates()
        super.onPause()
    }

    private fun doRequests(isOnline: Boolean) {
        if (isOnline) viewModel.resubscribeToRtUpdates()

        if (isOnline || isFirstStart) {
            progressDialog.show()
            viewModel.loadFxList(type, FxBaseCurrency.values()[tabLayout.selectedTabPosition])
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
}


