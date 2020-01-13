package com.juliusbaer.premarket.ui.company.performance

import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.tabs.TabLayout
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.dataFlow.NetworkStateManager
import com.juliusbaer.premarket.helpers.chart.ChartInterval
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.models.serverModels.ProductUpdateModel
import com.juliusbaer.premarket.models.serverModels.UnderlyingModel
import com.juliusbaer.premarket.ui.base.BaseNFragment
import com.juliusbaer.premarket.ui.base.NavigationHost
import com.juliusbaer.premarket.ui.chart.ChartActivity
import com.juliusbaer.premarket.ui.company.topWarrants.CompanyTopWarrantsFragment
import com.juliusbaer.premarket.ui.fragments.extentions.*
import com.juliusbaer.premarket.utils.Constants.DATE_FORMAT
import kotlinx.android.synthetic.main.fragment_performance.*
import kotlinx.android.synthetic.main.include_company_performance_values.*
import kotlinx.android.synthetic.main.include_price_info.*
import kotlinx.android.synthetic.main.layout_chart.*


class PerformanceFragment : BaseNFragment(R.layout.fragment_performance) {
    private val viewModel by viewModels<PerformanceViewModel> { viewModelFactory }

    companion object {
        private const val ARG_PRODUCT_ID = "productId"

        fun newInstance(collectionId: Int) = PerformanceFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_PRODUCT_ID, collectionId)
            }
        }

    }

    private val productId by lazy {
        requireArguments().getInt(ARG_PRODUCT_ID)
    }

    private var ticker = ""
    private var priceCurrency: String? = null

    private val periods = arrayListOf(ChartInterval.INTRADAY, ChartInterval.THREE_MONTH, ChartInterval.SIX_MONTH, ChartInterval.YEAR)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        for (period in periods) {
            tabLayout.addTab(tabLayout.newTab().setText(period.strResId), false)
        }
        val pos = periods.indexOf(storage.interval)
        tabLayout.getTabAt(pos)?.select()
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                updateBtn(periods[tab.position])
            }
        })
        txtOpenTop.setOnClickListener {
            (activity as? NavigationHost)?.openFragment(CompanyTopWarrantsFragment.newInstance(productId), null, true)
        }
        txtOpenTop.paintFlags = txtOpenTop.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        landingActionButton.setOnClickListener {
            startActivity(ChartActivity.newIntent(requireContext(), productId, ticker, periods, periods[tabLayout.selectedTabPosition]))
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.clearTopics()
        viewModel.itemLiveData.observe(viewLifecycleOwner, Observer {
            progressDialog.hide()
            when (it) {
                is Resource.Success -> {
                    populateUnderlyingUI(it.data)
                    viewModel.subscribeToRtUpdates(listOf(it.data.id))
                }
                is Resource.Failure -> {
                    it.data?.let { data -> populateUnderlyingUI(data) }
                    if (!it.hasBeenHandled) parseError(it.e()!!)
                }
            }
        })
        viewModel.productModelLiveData.observe(viewLifecycleOwner, Observer {
            uiUpdate(it)
        })
        viewModel.chartLiveData.observe(viewLifecycleOwner, Observer {
            progressDialog.hide()
            when (it) {
                is Resource.Success -> {
                    chart.clear()
                    val (result, period) = it.data
                    chart.setData(result.data?: emptyList(), period, result.xAxisInterval)
                }
                is Resource.Failure -> {
                    it.data?.let { (result, period) ->
                        chart.setData(result.data?: emptyList(), period, result.xAxisInterval)
                    }
                    if (!it.hasBeenHandled) parseError(it.e()!!)
                }
            }
        })
    }

    private fun populateUnderlyingUI(item: UnderlyingModel) {
        ticker = item.ticker
        priceCurrency = item.priceCurrency

        txtBidValueMarket.text = item.priceBid.format(2, item.priceCurrency!!)
        txtAskValueMarket.text = item.priceAsk.format(2, item.priceCurrency!!)
        txtVolumeValueInfo.setNumber(item.priceBidVolume!!)
        txtVolumeRightValue.setNumber(item.priceAskVolume!!)

        setPriceChangeUI(item.priceChangePct)

        txtOpeningValue.text = item.priceOpen?.format(2, item.priceCurrency)
        txtOpeningValue.isVisible = true
        txtOpening.isVisible = true
        txtValorValue.text = item.valor
        if (item.topWarrantsCount!! < 1) {
            txtOpenTop.isVisible = false
            txtTopWarrant.isVisible = false
        } else {
            txtOpenTop.isVisible = true
        }
        txtPriceValue.text = item.lastTraded?.format(2, item.priceCurrency)
        txtDateValues.text = item.priceDateTime?.formatDate(DATE_FORMAT) ?: ""
        txtRatio.text = "${item.maxLastTraded?.format(2)}/${item.minLastTraded?.format(2)} ${item.priceCurrency}"
        txtClosingValue.text = item.priceSettled?.format(2, item.priceCurrency)
    }

    private fun updateBtn(period: ChartInterval) {
        if (isFirstStart) progressDialog.show()
        viewModel.getChartDataResult(productId, period)
    }

    private fun uiUpdate(product: ProductUpdateModel) {
        txtBidValueMarket.text = product.priceBid.format(2, priceCurrency)
        txtAskValueMarket.text = product.priceAsk.format(2, priceCurrency)
        txtVolumeValueInfo.setNumber(product.priceBidVolume!!)
        txtVolumeRightValue.setNumber(product.priceAskVolume!!)

        setPriceChangeUI(product.priceChangePct)

        txtDateValues.text = product.priceDateTime?.formatDate(DATE_FORMAT) ?: ""
        txtRatio.text = "${product.maxLastTraded?.format(2)}/${product.minLastTraded?.format(2)} $priceCurrency"
        txtPriceValue.text = product.lastTraded?.format(2, priceCurrency)

        if (product.lastTraded != null && product.priceDateTime != null) {
            chart.updateDataChart(product.lastTraded, product.priceDateTime)
        }
    }

    private fun setPriceChangeUI(priceChangePct: Double) {
        txtPriceChange.text = priceChangePct.formatPercent(resources)
        val priceChangePctRounded = priceChangePct.times(100).round(2)
        val colorResId = when {
            priceChangePctRounded < 0 -> R.color.rouge
            priceChangePctRounded > 0 -> R.color.blueGreen
            else -> R.color.black
        }
        txtPriceChange.setTextColor(ContextCompat.getColor(requireContext(), colorResId))
    }

    private fun doRequests(isOnline: Boolean) {
        if (isOnline) viewModel.resubscribeToRtUpdates()

        if (isOnline || isFirstStart) {
            progressDialog.show()

            updateBtn(periods[tabLayout.selectedTabPosition])

            viewModel.loadUnderlying(productId)
        }
    }

    override fun onOnline() {
        doRequests(true)
    }

    override fun onResume() {
        super.onResume()

        doRequests(NetworkStateManager.isNetworkAvailable(requireContext()))
    }

    override fun onPause() {
        viewModel.unsubscribeFromRtUpdates()
        super.onPause()
    }
}