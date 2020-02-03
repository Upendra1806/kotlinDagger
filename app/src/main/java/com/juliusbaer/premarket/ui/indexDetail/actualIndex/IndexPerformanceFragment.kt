package com.juliusbaer.premarket.ui.indexDetail.actualIndex

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
import com.juliusbaer.premarket.models.serverModels.IndexModel
import com.juliusbaer.premarket.models.serverModels.ProductUpdateModel
import com.juliusbaer.premarket.ui.base.BaseNFragment
import com.juliusbaer.premarket.ui.chart.ChartActivity
import com.juliusbaer.premarket.ui.fragments.extentions.format
import com.juliusbaer.premarket.ui.fragments.extentions.formatDate
import com.juliusbaer.premarket.ui.fragments.extentions.formatPercent
import com.juliusbaer.premarket.ui.fragments.extentions.round
import com.juliusbaer.premarket.utils.Constants
import com.juliusbaer.premarket.utils.Constants.DATE_FORMAT
import kotlinx.android.synthetic.main.include_company_performance_values.*
import kotlinx.android.synthetic.main.include_performance_prices.*
import kotlinx.android.synthetic.main.layout_chart.*


class IndexPerformanceFragment : BaseNFragment(R.layout.fragment_index_performance) {
    private var mPosition =0

    companion object {
        private const val EXTRA_COLLECTION_ID = "collectionId"

        fun newInstance(productId: Int) = IndexPerformanceFragment().apply {
            arguments = Bundle().apply {
                putInt(EXTRA_COLLECTION_ID, productId)
            }
        }

    }

    private val productId by lazy {
        requireArguments().getInt(EXTRA_COLLECTION_ID)
    }

    private val viewModel by viewModels<IndexPerformanceViewModel> { viewModelFactory }

    private var priceCurrency: String? = null
    private var valor: String = ""
    private var ticker: String = ""

    private val periods = arrayListOf(ChartInterval.INTRADAY, ChartInterval.THREE_MONTH, ChartInterval.SIX_MONTH, ChartInterval.YEAR)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setItemVisibility()

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
                mPosition = tab.position
                updateBtn(periods[tab.position])
            }
        })
        landingActionButton.setOnClickListener {
            startActivity(ChartActivity.newIntent(requireContext(), productId, ticker, periods,
                    periods[tabLayout.selectedTabPosition],Constants.PRECISION,mChartType))
        }

        toggleGraph.setOnClickListener{
          toggleGraph()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.clearTopics()
        viewModel.indexLiveData.observe(viewLifecycleOwner, Observer {
            progressDialog.hide()

            when (it) {
                is Resource.Success -> {
                    updateUiFromApi(it.data)

                    viewModel.subscribeToRtUpdates(listOf(it.data.id))
                }
                is Resource.Failure -> {
                    it.data?.let { data -> updateUiFromApi(data) }
                    if (!it.hasBeenHandled) parseError(it.e()!!)
                }
            }
        })
        viewModel.productModelLiveData.observe(viewLifecycleOwner, Observer {
            viewModel.updateIndex(it)
            updateUiFromSocket(it)
        })
        showGraph(productId,periods[mPosition])
    }

    private fun setItemVisibility() {
        txtTopWarrant.isVisible = false
        txtOpenTop.isVisible = false
        txtOpeningValue?.isVisible = true
        txtOpening?.isVisible = true
        txtValorValue.isVisible = false
        txtValor.isVisible = false
    }

    private fun updateUiFromApi(item: IndexModel) {
        ticker = item.ticker!!
        priceCurrency = item.priceCurrency

        valor = item.valor

        setPriceChangeUI(item.priceChangePct)

        txtLastTrade.text = item.lastTraded?.format(2, item.priceCurrency)
        txtClosingValue.text = item.priceSettled?.format(2, item.priceCurrency)
        txtDate.text = item.date?.formatDate(DATE_FORMAT) ?: ""
        txtOpeningValue.text = item.priceOpen?.format(2, item.priceCurrency)
        txtRatio.text = "${item.maxLastTraded?.format(2)}/${item.minLastTraded?.format(2)} ${item.priceCurrency}"
    }

    private fun updateBtn(period: ChartInterval) {
        progressDialog.show()
        updateChart(productId,period)
    }

    private fun updateUiFromSocket(product: ProductUpdateModel) {
        setPriceChangeUI(product.priceChangePct)
        txtLastTrade.text = product.lastTraded?.format(2, priceCurrency)
        txtDate.text = product.priceDateTime?.formatDate(DATE_FORMAT) ?: ""
        txtRatio.text = "${product.maxLastTraded?.format(2)}/${product.minLastTraded?.format(2)} $priceCurrency"

        if (product.lastTraded != null && product.priceDateTime != null) {
            chart.updateDataChart(product.lastTraded, product.priceDateTime)
        }
    }

    private fun setPriceChangeUI(priceChangePct: Double) {
        txtPercent.text = priceChangePct.formatPercent(resources)

        val priceChangePcrRounded = priceChangePct.times(100).round(2)
        val colorResId = when {
            priceChangePcrRounded < 0 -> R.color.rouge
            priceChangePcrRounded > 0 -> R.color.blueGreen
            else -> R.color.text_grey_darker
        }
        txtPercent.setTextColor(ContextCompat.getColor(requireContext(), colorResId))
    }

    override fun onPause() {
        viewModel.unsubscribeFromRtUpdates()
        super.onPause()
    }

    override fun onOnline() {
        doRequests(true)
    }

    private fun doRequests(isOnline: Boolean) {
        if (isOnline) viewModel.resubscribeToRtUpdates()

        if (isFirstStart || isOnline) {
            progressDialog.show()
            viewModel.loadIndex(productId)

            updateBtn(periods[tabLayout.selectedTabPosition])
        }
    }

    override fun onResume() {
        super.onResume()

        doRequests(NetworkStateManager.isNetworkAvailable(requireContext()))
    }
}