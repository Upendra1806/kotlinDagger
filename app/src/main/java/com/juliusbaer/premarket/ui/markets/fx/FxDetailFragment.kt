package com.juliusbaer.premarket.ui.markets.fx

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.tabs.TabLayout
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.dataFlow.NetworkStateManager
import com.juliusbaer.premarket.helpers.chart.ChartInterval
import com.juliusbaer.premarket.models.ProductType
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.models.serverModels.FxModel
import com.juliusbaer.premarket.models.serverModels.FxType
import com.juliusbaer.premarket.models.serverModels.ProductUpdateModel
import com.juliusbaer.premarket.stat.StatisticsManager
import com.juliusbaer.premarket.ui.alerts.UnderlyingAlertsActivity
import com.juliusbaer.premarket.ui.base.BaseNFragment
import com.juliusbaer.premarket.ui.base.HasOfflinePlaceHolder
import com.juliusbaer.premarket.ui.chart.ChartActivity
import com.juliusbaer.premarket.ui.fragments.extentions.*
import com.juliusbaer.premarket.ui.login.LoginActivity
import com.juliusbaer.premarket.utils.Constants
import com.juliusbaer.premarket.utils.FxMetals
import kotlinx.android.synthetic.main.fragment_fx_detail.*
import kotlinx.android.synthetic.main.header_app.screenTitle
import kotlinx.android.synthetic.main.include_performance_prices.*
import kotlinx.android.synthetic.main.include_toolbar.*
import kotlinx.android.synthetic.main.layout_chart.*
import javax.inject.Inject


class FxDetailFragment : BaseNFragment(R.layout.fragment_fx_detail), HasOfflinePlaceHolder {
    companion object {
        private const val ARG_PRODUCT_ID = "productId"
        private const val ARG_MODEL = "model"
        private const val ARG_TYPE = "type"
        private const val ARG_IS_PUSH = "isPush"

        fun newInstance(productId: Int, type: FxType, isPush: Boolean = false, model: FxModel? = null) = FxDetailFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_PRODUCT_ID, productId)
                putSerializable(ARG_TYPE, type)
                putBoolean(ARG_IS_PUSH, isPush)
                model?.let { putParcelable(ARG_MODEL, it) }
            }
        }

    }

    private var precision: Int = 4

    private val model by lazy {
        arguments?.getParcelable<FxModel>(ARG_MODEL)
    }

    private val productId by lazy {
        requireArguments().getInt(ARG_PRODUCT_ID)
    }

    private val type by lazy {
        requireArguments().getSerializable(ARG_TYPE) as FxType
    }

    private val isPush by lazy {
        arguments?.getBoolean(ARG_IS_PUSH) ?: false
    }

    private val viewModel by viewModels<FxDetailViewModel> { viewModelFactory }

    private var ticker: String? = null
    private var title: String = ""

    private var isImgStarTrue = false

    private var hasDetailData = false

    @Inject
    lateinit var statisticsManager: StatisticsManager

    private val periods = arrayListOf(ChartInterval.INTRADAY, ChartInterval.ONE_MONTH, ChartInterval.THREE_MONTH, ChartInterval.SIX_MONTH)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        precision = model?.precision ?: 4
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initToolbar()

        toolbar.inflateMenu(R.menu.company)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.call -> {
                    callToTrader()
                    true
                }
                R.id.set_alert -> {
                    if (viewModel.isConfirmed()) {
                        if (NetworkStateManager.isNetworkAvailable(requireContext())) {
                            startActivity(UnderlyingAlertsActivity.newIntent(requireContext(),
                                    productId,
                                    if (type == FxType.CURRENCY) ProductType.CURRENCY else ProductType.METAL,
                                    precision))
                        }
                    } else {
                        context?.startActivity(Intent(context, LoginActivity::class.java))
                    }
                    true
                }
                R.id.add_to_watchlist -> {
                    if (NetworkStateManager.isNetworkAvailable(requireContext())) setAlertState()

                    true
                }
                else -> false
            }
        }
        for (period in periods) {
            tabLayout.addTab(tabLayout.newTab().setText(period.strResId), false)
        }
        val pos = periods.indexOf(storage.fxInterval)
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
        landingActionButton.setOnClickListener {
            startActivity(ChartActivity.newIntent(requireContext(), productId, title, periods, periods[tabLayout.selectedTabPosition], precision))
        }
        chart.precision = precision
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (viewModel.itemLiveData.value == null) {
            model?.let {
                updateUIFromApi(it)
            }
        }
        viewModel.clearTopics()
        viewModel.itemLiveData.observe(viewLifecycleOwner, Observer {
            progressDialog.hide()

            when (it) {
                is Resource.Success -> {
                    updateUIFromApi(it.data)

                    viewModel.subscribeToRtUpdates(listOf(it.data.id))
                }
                is Resource.Failure -> {
                    it.data?.let { data ->
                        updateUIFromApi(data)
                    }
                    if (!it.hasBeenHandled) parseError(it.e()!!)
                }
            }
        })
        viewModel.productModelLiveData.observe(viewLifecycleOwner, Observer {
            updateUiFromSocket(it)
        })
        viewModel.chartLiveData.observe(viewLifecycleOwner, Observer {
            progressDialog.hide()
            when (it) {
                is Resource.Success -> {
                    chart.clear()

                    val (result, period) = it.data
                    chart.setData(result.data ?: emptyList(), period, result.xAxisInterval)
                }
                is Resource.Failure -> {
                    it.data?.let { (result, period) ->
                        chart.setData(result.data ?: emptyList(), period, result.xAxisInterval)
                    }
                    if (!it.hasBeenHandled) parseError(it.e()!!)
                }
            }
        })
        viewModel.watchListDelLiveData.observe(viewLifecycleOwner, Observer {
            progressDialog.hide()
            isImgStarTrue = false

            toolbar.menu.findItem(R.id.add_to_watchlist).setIcon(R.drawable.ic_watchlist)
        })
        viewModel.watchListAddLiveData.observe(viewLifecycleOwner, Observer {
            progressDialog.hide()
            when (it) {
                is Resource.Success -> {
                    if (!isImgStarTrue) {
                        toolbar.menu.findItem(R.id.add_to_watchlist).setIcon(R.drawable.ic_watchlist_active)
                    }
                    isImgStarTrue = true
                }
                is Resource.Failure -> if (!it.hasBeenHandled) parseError(it.e()!!, true)
            }
        })
        statisticsManager.onProductStart(productId, 0, if (isPush) 1 else 0)
    }

    private fun updateUIFromApi(item: FxModel) {
        hasDetailData = item.isInWatchList != null

        ticker = item.ticker

        FxMetals.values().firstOrNull {
            item.ticker?.startsWith(it.prefix, ignoreCase = true) == true
        }?.let {
            pairHumanName.setText(it.humanTitleResId)
            pairHumanName.setCompoundDrawablesRelativeWithIntrinsicBounds(it.iconResId, 0, 0, 0)
            pairHumanName.isVisible = true
        }
        title = item.title

        precision = item.precision

        screenTitle.text = item.name
        isImgStarTrue = item.isInWatchList ?: false
        if (isImgStarTrue) {
            toolbar.menu.findItem(R.id.add_to_watchlist).setIcon(R.drawable.ic_watchlist_active)
        }
        daysMovingAverage50.text = item.averagePriceFor50days?.format(item.precision)
        daysMovingAverage100.text = item.averagePriceFor100days?.format(item.precision)
        daysMovingAverage200.text = item.averagePriceFor200days?.format(item.precision)

        setPriceChangeUI(item.priceChangePct)

        txtLastTrade.text = item.lastTraded?.format(item.precision)
        txtDate.text = item.priceDateTime?.formatDate(Constants.DATE_FORMAT) ?: ""
        txtDate.text = item.priceDateTime?.formatDate(Constants.DATE_FORMAT) ?: ""
        val ratio = StringBuilder()
        if (item.maxLastTraded != null) ratio.append(item.maxLastTraded!!.format(item.precision))
        if (item.minLastTraded != null) {
            if (item.maxLastTraded != null) ratio.append("/")
            ratio.append(item.minLastTraded!!.format(item.precision))
        }
        txtRatio.text = ratio.toString()

    }

    private fun updateBtn(period: ChartInterval) {
        progressDialog.show()
        viewModel.getChartDataResult(productId, period)
    }

    private fun updateUiFromSocket(product: ProductUpdateModel) {
        setPriceChangeUI(product.priceChangePct)
        txtLastTrade.text = product.lastTraded?.format(precision)
        txtDate.text = product.priceDateTime?.formatDate(Constants.DATE_FORMAT) ?: ""

        val ratio = StringBuilder()
        if (product.maxLastTraded != null) ratio.append(product.maxLastTraded.format(precision))
        if (product.minLastTraded != null) {
            if (product.maxLastTraded != null) ratio.append("/")
            ratio.append(product.minLastTraded.format(precision))
        }
        txtRatio.text = ratio.toString()

        if (product.lastTraded != null && product.priceDateTime != null) {
            chart.updateDataChart(product.lastTraded, product.priceDateTime)
        }
    }

    private fun setPriceChangeUI(priceChangePct: Double) {
        txtPercent.text = priceChangePct.formatPercent(resources)
        val priceChangePctRounded = priceChangePct.times(100).round(precision)
        val colorResId = when {
            priceChangePctRounded < 0 -> R.color.rouge
            priceChangePctRounded > 0 -> R.color.blueGreen
            else -> R.color.text_grey_darker
        }
        txtPercent.setTextColor(ContextCompat.getColor(requireContext(), colorResId))
    }

    override fun onPause() {
        viewModel.unsubscribeFromRtUpdates()
        super.onPause()
    }

    override fun onOffline() {
        if (!hasDetailData) {
            super.onOffline()
        }
    }

    override fun onOnline() {
        doRequests(true)
    }

    private fun doRequests(isOnline: Boolean) {
        if (isOnline) viewModel.resubscribeToRtUpdates()

        if (isFirstStart || isOnline) {
            progressDialog.show()
            viewModel.loadFxModel(productId, type)

            updateBtn(periods[tabLayout.selectedTabPosition])
        }
    }

    override fun onResume() {
        super.onResume()

        doRequests(NetworkStateManager.isNetworkAvailable(requireContext()))
    }

    private fun setAlertState() {
        if (!isImgStarTrue) {
            if (viewModel.isTokenValid()) {
                statisticsManager.onProductStart(productId, 1, if (isPush) 1 else 0)
                progressDialog.show()
                viewModel.addWatchListItem(productId)
            } else {
                Toast.makeText(context, "Is not authorized", Toast.LENGTH_SHORT).show()
            }
        } else {
            statisticsManager.onProductStart(productId, 2, if (isPush) 1 else 0)
            progressDialog.show()
            viewModel.deleteWatchListItemResponse(productId)

        }
    }
}