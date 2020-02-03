package com.juliusbaer.premarket.ui.base


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.core.viewmodel.ViewModelFactory
import com.juliusbaer.premarket.dataFlow.IUserStorage
import com.juliusbaer.premarket.dataFlow.NetworkStateManager
import com.juliusbaer.premarket.helpers.chart.ChartInterval
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.ui.OfflineFragment
import com.juliusbaer.premarket.ui.ProgressDialog
import com.juliusbaer.premarket.ui.chart.BaseChartViewModel
import com.juliusbaer.premarket.ui.fragments.extentions.replaceChildFragmentSafely
import com.juliusbaer.premarket.ui.login.LoginActivity
import com.juliusbaer.premarket.ui.phoneDialog.PhoneConfirmDialogFragment
import com.juliusbaer.premarket.ui.phoneDialog.PhoneDialogFragment
import com.juliusbaer.premarket.utils.Constants
import com.juliusbaer.premarket.utils.UiUtils
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.layout_chart.*
import timber.log.Timber
import javax.inject.Inject

abstract class BaseNFragment(@LayoutRes layoutId: Int) : Fragment(layoutId), HasAndroidInjector {
    protected var isFirstStart: Boolean = true

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    protected val userInfoViewModel by viewModels<UserInfoViewModel> { viewModelFactory }

    @Inject
    lateinit var storage: IUserStorage

    private var canBeClicked = true

    protected val progressDialog by lazy { ProgressDialog.progressDialog(requireContext(), false) }

    @Inject
    lateinit var networkStateManager: NetworkStateManager

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector(): AndroidInjector<Any> {
        return androidInjector
    }

    @Inject lateinit var graphViewMode:BaseChartViewModel

     var mChartType = Constants.ChartConstant.LINE_CHART


    fun parseError(it: Throwable, forceAlerts: Boolean = false) {
        Timber.e(it)
        UiUtils.parseError(it, requireActivity(), childFragmentManager, storage, forceAlerts) { setOffline() }
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    private fun setOffline() {
        networkStateManager.setOffline()

        if (this is HasOfflinePlaceHolder && childFragmentManager.findFragmentByTag(Constants.OFFLINE_FRAGMENT) == null) {
            onOffline()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        networkStateManager.networkStateLiveData.observe(viewLifecycleOwner, Observer { isOnline ->
            if (isOnline) {
                childFragmentManager.findFragmentByTag(Constants.OFFLINE_FRAGMENT)?.let { fragment ->
                    childFragmentManager.commit { remove(fragment) }
                }
                onOnline()
            }
        })
        userInfoViewModel.userInfoLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> callToTraderInternal(it.data.traderPhoneNumber ?: "")
                is Resource.Failure -> Timber.e(it.e())
            }
        })
    }

    protected open fun onOffline() {
        view?.findViewById<View>(R.id.contentHolder)?.let {
            replaceChildFragmentSafely(OfflineFragment(), Constants.OFFLINE_FRAGMENT, false, R.id.contentHolder)
        }
    }

    protected open fun onOnline() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        canBeClicked = true
    }

    override fun onPause() {
        isFirstStart = false
        super.onPause()
    }

    override fun onStop() {
        progressDialog.dismiss()
        super.onStop()
    }

    fun callToTrader() {
        if (!storage.isConfirmed()) {
            startActivity(Intent(context, LoginActivity::class.java))
            return
        }
        val number = storage.getPhoneNumber()
        if (number == null) {
            userInfoViewModel.syncUserInfo()
        } else {
            callToTraderInternal(number)
        }
    }

    private fun callToTraderInternal(number: String) {
        if (number.isBlank()) {
            PhoneDialogFragment().show(childFragmentManager, "number")
        } else {
            callPhone(number)
        }
    }

    fun callPhone(number: String) {
        PhoneConfirmDialogFragment.newInstance(number).show(childFragmentManager, "call")
    }

    fun showGraph(productId: Int,period: ChartInterval){
        graphViewMode.chartLiveData.observe(viewLifecycleOwner, Observer {
            progressDialog.hide()
            when (it) {
                is Resource.Success -> {
                    chart.clear()
                    val (result, period) = it.data
                    chart.setData(result.data ?: emptyList(), period, result.xAxisInterval)
                }
                is Resource.Failure -> {
                    it.data?.let { (result, period) ->
                        chart.setData(result.data?: emptyList(), period, result.xAxisInterval)
                    }
                    if (!it.hasBeenHandled) parseError(it.e()!!)
                }
            }
        })
        graphViewMode.candleChartLiveData.observe(viewLifecycleOwner, Observer {
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

        graphViewMode.getChartType().observe(viewLifecycleOwner, Observer {
            mChartType = it
            updateChart(productId,period)
        })
    }

    fun setCurrentChartType(type: String){
        graphViewMode.setChartType(type)
    }

    fun toggleGraph(){
        when(mChartType){
            Constants.ChartConstant.LINE_CHART->{
                chart.visibility = View.INVISIBLE
                candleChart.visibility = View.VISIBLE
                toggleGraph.setImageResource(R.drawable.ic_boxes)
                setCurrentChartType(Constants.ChartConstant.CANDLE_CHART)

            }
            Constants.ChartConstant.CANDLE_CHART->{
                chart.visibility = View.VISIBLE
                candleChart.visibility = View.INVISIBLE
                toggleGraph.setImageResource(R.drawable.ic_candlestick)
                setCurrentChartType(Constants.ChartConstant.LINE_CHART)
            }
        }
    }

    fun updateChart(productId: Int,period: ChartInterval){
        when(mChartType)
        {
            Constants.ChartConstant.LINE_CHART->{
                graphViewMode.getChartDataResult(productId, period)
            }
            Constants.ChartConstant.CANDLE_CHART->{
                graphViewMode.getCandleChartDataResult(productId, period)
            }
        }
    }

    fun setPrecision(precision: Int){
        chart.precision = precision
    }
}