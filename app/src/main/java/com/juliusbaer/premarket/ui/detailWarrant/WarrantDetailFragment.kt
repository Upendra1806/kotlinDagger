package com.juliusbaer.premarket.ui.detailWarrant


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.dataFlow.NetworkStateManager
import com.juliusbaer.premarket.models.DetailWarrModel
import com.juliusbaer.premarket.models.ProductType
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.models.serverModels.ProductUpdateModel
import com.juliusbaer.premarket.models.serverModels.WarrantModel
import com.juliusbaer.premarket.stat.StatisticsManager
import com.juliusbaer.premarket.ui.alerts.UnderlyingAlertsActivity
import com.juliusbaer.premarket.ui.base.BaseNFragment
import com.juliusbaer.premarket.ui.base.HasOfflinePlaceHolder
import com.juliusbaer.premarket.ui.fragments.extentions.*
import com.juliusbaer.premarket.ui.login.LoginActivity
import com.juliusbaer.premarket.utils.Constants
import kotlinx.android.synthetic.main.child_header_app.*
import kotlinx.android.synthetic.main.fragment_warrants_detail.*
import kotlinx.android.synthetic.main.include_price_info.*
import kotlinx.android.synthetic.main.include_toolbar.*
import javax.inject.Inject


class WarrantDetailFragment : BaseNFragment(R.layout.fragment_warrants_detail), HasOfflinePlaceHolder {
    @Inject
    lateinit var statisticsManager: StatisticsManager

    private val viewModel by viewModels<WarrantsDetailViewModel> { viewModelFactory }

    companion object {
        private const val ARG_COLLECTION_ID = "collectionId"
        private const val ARG_WARRANT = "collectionId"
        private const val ARG_PUSH_ID = "isPush"

        fun newInstance(id: Int, isPush: Boolean = false, warrant: WarrantModel? = null) = WarrantDetailFragment().apply {
            arguments = Bundle().apply {
                warrant?.let { putSerializable(ARG_WARRANT, it) }
                putInt(ARG_COLLECTION_ID, id)
                putBoolean(ARG_PUSH_ID, isPush)
            }
        }

    }

    private val collectionId by lazy {
        requireArguments().getInt(ARG_COLLECTION_ID)
    }

    private val isPush by lazy {
        arguments?.getBoolean(ARG_PUSH_ID) ?: false
    }

    private var isImgStarTrue = false
    private var priceCurrency: String? = null

    private val warrant by lazy {
        arguments?.getSerializable(ARG_WARRANT) as? WarrantModel
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        warrant?.let {
            outState.putSerializable(ARG_WARRANT, warrant)
        }
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
                            startActivity(UnderlyingAlertsActivity.newIntent(requireContext(), collectionId, ProductType.WARRANT))
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
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.clearTopics()
        viewModel.itemLiveData.observe(viewLifecycleOwner, Observer {
            progressDialog.hide()
            when (it) {
                is Resource.Success -> {
                    setUI(it.data)
                    viewModel.subscribeToRtUpdates(listOf(it.data.id))
                }
                is Resource.Failure -> {
                    it.data?.let { setUI(it) }
                    if (!it.hasBeenHandled) parseError(it.e()!!)
                }
            }
        })
        viewModel.productModelLiveData.observe(viewLifecycleOwner, Observer { product ->
            viewModel.updateWarrant(product)
            updateUI(product)
        })
        viewModel.watchListItemAdd.observe(viewLifecycleOwner, Observer {
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
        viewModel.watchListItemDel.observe(viewLifecycleOwner, Observer {
            progressDialog.hide()
            isImgStarTrue = false
            toolbar.menu.findItem(R.id.add_to_watchlist).setIcon(R.drawable.ic_watchlist)
        })
        statisticsManager.onProductStart(collectionId, 0, if (isPush) 1 else 0)
    }

    override fun onResume() {
        super.onResume()

        doRequests(NetworkStateManager.isNetworkAvailable(requireContext()))
    }

    override fun onOnline() {
        doRequests(true)
    }

    override fun onOffline() {
        if (screenTitle.text.isEmpty()) {
            super.onOffline()
        }
    }

    private fun doRequests(isOnline: Boolean) {
        if (isOnline) viewModel.resubscribeToRtUpdates()

        if (isOnline || isFirstStart) {
            progressDialog.show()
            viewModel.getDetailWarrant(collectionId)
        }
    }

    private fun setUI(item: DetailWarrModel) {
        screenTitle.text = item.ticker
        isImgStarTrue = item.isInWatchList!!
        if (isImgStarTrue) {
            toolbar.menu.findItem(R.id.add_to_watchlist).setIcon(R.drawable.ic_watchlist_active)
        }
        txtBidValueMarket.text = "${item.priceBid.format(2)} ${item.priceCurrency}"
        txtAskValueMarket.text = "${item.priceAsk.format(2)} ${item.priceCurrency}"

        setPriceChangeUI(item.priceChangePct)

        txtValorValue.text = item.valor
        txtVolumeValueInfo.setNumber(item.priceBidVolume)
        txtVolumeRightValue.setNumber(item.priceAskVolume)
        expiryDate.text = item.exerciseDate?.formatDate(Constants.DATE_ONLY_FORMAT) ?: ""
        txtExerciseDate.text = item.priceSettled?.format(2)

        txtPriceValue.text = item.lastTraded?.format(2, item.priceCurrency)
        txtDateValues.text = item.priceDateTime?.formatDate(Constants.DATE_FORMAT) ?: ""
        priceCurrency = item.priceCurrency
        txtPriceCurrency.text = priceCurrency

        category.isVisible = when (item.category) {
            Constants.WARRANTS_CATEGORY_KNOCK_OUT -> {
                category.setText(R.string.knock_out)
                true
            }
            Constants.WARRANTS_CATEGORY_RANGE -> {
                category.setText(R.string.range)
                true
            }
            else -> false
        }
        if (item.category == Constants.WARRANTS_CATEGORY_RANGE) {
            cap.text = item.barrierLevel?.format(2)
            floor.text = item.barrierUpperLevel?.format(2)
            daysWithRange.text = item.eventsInRange?.toString()
            tradingDaysToMaturity.text = item.tradingDaysToMaturity?.toString()
            maxPayout.text = item.paymentCapLevel?.format(2)
            dailyPayment.text = item.paymentAmount?.format(2)

            setPutCallFieldsVisibility(false)
            setRangeWarrantFieldsVisibility(true)
        } else {
            txtStrikeLevel.text = item.strikeLevel?.format(2)
            txtRatio.text = item.ratio
            txtImpliedVolatility.text = item.impliedVolatility?.formatPercent(resources, false)
            txtDelta.text = item.delta?.format(2)
            txtLeverage.text = item.leverage?.format(2)
            txtGearing.text = item.gearing?.format(2)

            setPutCallFieldsVisibility(true)
            setRangeWarrantFieldsVisibility(false)

            when (item.strikeType?.toLowerCase()) {
                "put" -> {
                    txtPutCall.setBackgroundResource(R.drawable.bg_round_rouge)
                    txtPutCall.setText(R.string.put)
                }
                "call" -> {
                    txtPutCall.setBackgroundResource(R.drawable.bg_round_blue_green)
                    txtPutCall.setText(R.string.call)
                }
                else -> {
                    txtPutCall.visibility = View.INVISIBLE
                }
            }
        }
        txtParentName.text = item.parentName
        txtTradedVolume.text = item.tradedVolume?.toString()
    }

    private fun setPriceChangeUI(priceChangePct: Double) {
        txtPriceChange.text = priceChangePct.formatPercent(resources)
        val priceChangePctRounded = priceChangePct.times(100).round(2)

        val colorResId = when {
            priceChangePctRounded < 0 -> R.color.rouge
            priceChangePctRounded > 0 -> R.color.blueGreen
            else -> R.color.text_grey_darker
        }
        txtPriceChange.setTextColor(ContextCompat.getColor(requireContext(), colorResId))
    }
    
    private fun setRangeWarrantFieldsVisibility(isVisible: Boolean) {
        capLabel.isVisible = isVisible
        cap.isVisible = isVisible

        floorLabel.isVisible = isVisible
        floor.isVisible = isVisible

        daysWithRangeLabel.isVisible = isVisible
        daysWithRange.isVisible = isVisible

        maxPayoutLabel.isVisible = isVisible
        maxPayout.isVisible = isVisible

        dailyPaymentLabel.isVisible = isVisible
        dailyPayment.isVisible = isVisible

        tradingDaysToMaturityLabel.isVisible = isVisible
        tradingDaysToMaturity.isVisible = isVisible
    }

    private fun setPutCallFieldsVisibility(isVisible: Boolean) {
        txtPutCall.isVisible = isVisible

        strikeLabel.isVisible = isVisible
        txtStrikeLevel.isVisible = isVisible

        ratioLabel.isVisible = isVisible
        txtRatio.isVisible = isVisible

        volatilityLabel.isVisible = isVisible
        txtImpliedVolatility.isVisible = isVisible

        deltaLabel.isVisible = isVisible
        txtDelta.isVisible = isVisible

        leverageLabel.isVisible = isVisible
        txtLeverage.isVisible = isVisible

        gearingLabel.isVisible = isVisible
        txtGearing.isVisible = isVisible
    }

    private fun updateUI(product: ProductUpdateModel) {
//        name.text = product.identifier
        txtBidValueMarket.text = "${product.priceBid.format(2)} $priceCurrency"
        txtAskValueMarket.text = "${product.priceAsk.format(2)} $priceCurrency"

        setPriceChangeUI(product.priceChangePct)

        txtVolumeValueInfo.setNumber(product.priceBidVolume!!)
        txtVolumeRightValue.setNumber(product.priceAskVolume!!)
        txtPriceValue.text = product.lastTraded?.format(2, priceCurrency)
        txtExerciseDate.text = product.priceSettled?.format(2)
        txtDateValues.text = product.priceDateTime?.formatDate(Constants.DATE_FORMAT) ?: ""
        txtImpliedVolatility.text = product.impliedVolatility?.formatPercent(resources, false)
        txtDelta.text = product.delta?.format(2)
        txtLeverage.text = product.leverage?.format(2)
        txtGearing.text = product.gearing?.format(2)
        txtTradedVolume.text = product.tradedVolume?.toString()?:""
    }

    private fun setAlertState() {
        if (!isImgStarTrue) {
            if (viewModel.isTokenValid()) {
                statisticsManager.onProductStart(collectionId, 1, if (isPush) 1 else 0)
                progressDialog.show()
                viewModel.addWatchListItem(collectionId)
            } else {
                Toast.makeText(context, "Is not authorized", Toast.LENGTH_SHORT).show()
            }
        } else {
            statisticsManager.onProductStart(collectionId, 2, if (isPush) 1 else 0)
            progressDialog.show()
            viewModel.deleteWatchListItemResponse(collectionId)
        }
    }

    override fun onPause() {
        viewModel.unsubscribeFromRtUpdates()
        super.onPause()
    }
}
