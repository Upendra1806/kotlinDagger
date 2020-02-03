package com.juliusbaer.premarket.ui.chart

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.tabs.TabLayout
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.dataFlow.NetworkStateManager
import com.juliusbaer.premarket.helpers.chart.ChartInterval
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.ui.base.BaseNFragment
import com.juliusbaer.premarket.ui.base.HasOfflinePlaceHolder
import com.juliusbaer.premarket.utils.Constants
import kotlinx.android.synthetic.main.landing_chart_fragment.*
import kotlinx.android.synthetic.main.landing_chart_fragment.chart
import kotlinx.android.synthetic.main.landing_chart_fragment.tabLayout
import kotlinx.android.synthetic.main.landing_chart_fragment.toggleGraph

class LandingChartFragment : BaseNFragment(R.layout.landing_chart_fragment), HasOfflinePlaceHolder {
    companion object {
        private const val ARG_COLLECTION_ID = "collectionId"
        private const val ARG_PRECISION = "precision"
        private const val ARG_TITLE = "title"
        private const val ARG_PERIODS = "periods"
        private const val ARG_INTERVAL = "interval"
        private const val ARG_CHART_TYPE = "chartType"

        fun newInstance(itemId: Int,
                        title: String,
                        periods: ArrayList<ChartInterval>,
                        period: ChartInterval,
                        precision: Int = Constants.PRECISION, chartType: String) = LandingChartFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_COLLECTION_ID, itemId)
                putInt(ARG_PRECISION, precision)
                putString(ARG_TITLE, title)
                putSerializable(ARG_PERIODS, periods)
                putSerializable(ARG_INTERVAL, period)
                putSerializable(ARG_CHART_TYPE, chartType)
            }
        }
    }

    private val viewModel by viewModels<BaseChartViewModel> { viewModelFactory }

    private val collectionId by lazy {
        requireArguments().getInt(ARG_COLLECTION_ID)
    }

    private val title by lazy {
        requireArguments().getString(ARG_TITLE)
    }

    private val interval by lazy {
        requireArguments().getSerializable(ARG_INTERVAL) as ChartInterval
    }

    private val periods by lazy {
        requireArguments().getSerializable(ARG_PERIODS) as List<ChartInterval>
    }
    private var currentPosition = 0
    private var chartType = Constants.ChartConstant.LINE_CHART

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        titleChart.text = title
        chartType = requireArguments().getString(ARG_CHART_TYPE)


        for (period in periods) {
            tabLayout.addTab(tabLayout.newTab().setText(period.strResId), false)
        }
        val pos = periods.indexOf(interval)
        currentPosition = pos
        tabLayout.getTabAt(pos)?.select()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                currentPosition = tab.position
                updateBtn(periods[currentPosition])
            }
        })
        toggleGraph.setOnClickListener {
            when (mChartType) {
                Constants.ChartConstant.LINE_CHART -> {
                    chart.visibility = View.INVISIBLE
                    candleChart.visibility = View.VISIBLE
                    toggleGraph.setImageResource(R.drawable.ic_boxes)
                    chartType = Constants.ChartConstant.CANDLE_CHART
                    updateBtn(periods[currentPosition])
                }
                Constants.ChartConstant.CANDLE_CHART -> {
                    chart.visibility = View.VISIBLE
                    candleChart.visibility = View.INVISIBLE
                    toggleGraph.setImageResource(R.drawable.ic_candlestick)
                    chartType = Constants.ChartConstant.CANDLE_CHART
                    updateBtn(periods[currentPosition])
                }
            }
        }
        iconBack.setOnClickListener { activity?.onBackPressed() }
        chart.precision = arguments?.getInt(ARG_PRECISION) ?: Constants.PRECISION
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.chartLiveData.observe(viewLifecycleOwner, Observer {
            progressDialog.hide()
            when (it) {
                is Resource.Success -> {
                    chart.clear()

                    val (result, period) = it.data
                    chart.setData(result.data ?: emptyList(), period, result.xAxisInterval)
                    viewModel.subscribeToRtUpdates(listOf(collectionId))
                }
                is Resource.Failure -> {
                    it.data?.let { (result, period) ->
                        chart.setData(result.data ?: emptyList(), period, result.xAxisInterval)
                    }
                    if (!it.hasBeenHandled) parseError(it.e()!!)
                }
            }
        })

        viewModel.candleChartLiveData.observe(viewLifecycleOwner, Observer {
            progressDialog.hide()
            when (it) {
                is Resource.Success -> {
                    //TODO need to handle
                }
                is Resource.Failure -> {
                    //TODO need to handle
                    if (!it.hasBeenHandled) parseError(it.e()!!)
                }
            }

        })
        viewModel.productModelLiveData.observe(viewLifecycleOwner, Observer { product ->
            if (product.lastTraded != null && product.priceDateTime != null) {
                chart.updateDataChart(product.lastTraded, product.priceDateTime)
            }
        })
    }

    private fun updateBtn(period: ChartInterval) {
        if (isFirstStart) progressDialog.show()
        when (chartType) {
            Constants.ChartConstant.LINE_CHART -> {
                viewModel.getChartDataResult(collectionId, period)
            }
            Constants.ChartConstant.CANDLE_CHART -> {
                viewModel.getCandleChartDataResult(collectionId, period)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (NetworkStateManager.isNetworkAvailable(requireContext()) || isFirstStart) doRequests()
    }

    private fun doRequests() {
        viewModel.resubscribeToRtUpdates()

        updateBtn(periods[tabLayout.selectedTabPosition])
    }

    override fun onOnline() {
        doRequests()
    }

    override fun onOffline() {
        if (chart.data?.dataSetCount ?: 0 > 0) {
            super.onOffline()
        }
    }

    override fun onPause() {
        viewModel.unsubscribeFromRtUpdates()
        super.onPause()
    }
}
