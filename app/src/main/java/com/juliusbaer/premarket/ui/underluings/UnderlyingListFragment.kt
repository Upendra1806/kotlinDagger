package com.juliusbaer.premarket.ui.underluings


import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.dataFlow.NetworkStateManager
import com.juliusbaer.premarket.dataFlow.zeroMQ.SubscriptionPrefix
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.models.serverModels.*
import com.juliusbaer.premarket.ui.base.BaseNFragment
import com.juliusbaer.premarket.ui.base.HasOfflinePlaceHolder
import com.juliusbaer.premarket.ui.base.NavigationHost
import com.juliusbaer.premarket.ui.company.CompanyFragment
import com.juliusbaer.premarket.ui.fragments.extentions.format
import com.juliusbaer.premarket.ui.fragments.extentions.formatDate
import com.juliusbaer.premarket.ui.fragments.extentions.formatPercent
import com.juliusbaer.premarket.ui.indexDetail.IndexDetailFragment
import com.juliusbaer.premarket.utils.Constants.DATE_FORMAT
import com.juliusbaer.premarket.utils.Util
import kotlinx.android.synthetic.main.fragment_underlying_list.*
import kotlinx.android.synthetic.main.market_card_candle.*

class UnderlyingListFragment : BaseNFragment(R.layout.fragment_underlying_list), HasOfflinePlaceHolder {
    companion object {
        private const val ARG_TYPE = "type"

        fun newInstance(type: UnderlyingType): UnderlyingListFragment {
            return UnderlyingListFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_TYPE, type)
                }
            }
        }
    }

    private val type by lazy {
        requireArguments().getSerializable(ARG_TYPE) as UnderlyingType
    }

    private val viewModel by viewModels<UnderlyingListViewModel> { viewModelFactory }

    private val parentViewModel by lazy {
        ViewModelProviders.of(parentFragment!!, viewModelFactory).get(UnderlyingsViewModel::class.java)
    }

    private var candlesMin = 0
    private var candlesMax = 0
    private var indexCandleStick: CandleStickModel? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerViewHome.setHasFixedSize(true)

        val gradientDrawable = GradientDrawable()
        gradientDrawable.shape = GradientDrawable.RECTANGLE
        gradientDrawable.cornerRadius = resources.getDimension(R.dimen.underlying_square_radius)
        cardView2.background = gradientDrawable
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.clearTopics()

        parentViewModel.boxesLiveData.observe(viewLifecycleOwner, Observer { isBoxes ->
            if (setRecyclerAutomatically(isBoxes)) {
                viewModel.refreshLiveData(isBoxes)
            }
        })
        viewModel.underlyingsLiveData.observe(viewLifecycleOwner, Observer {
            spinner.isVisible = false
            when (it) {
                is Resource.Success -> {
                    populateUnderlyingUI(it.data)
                    if (it.data.isNotEmpty()) {
                        viewModel.subscribeToRtUpdates(it.data.map { model -> model.id }, SubscriptionPrefix.PRODUCT)
                    }
                }
                is Resource.Failure -> {
                    it.data?.let { data -> populateUnderlyingUI(data) }
                    if (!it.hasBeenHandled) parseError(it.e()!!)
                }
            }
        })
        viewModel.candleSticksLiveData.observe(viewLifecycleOwner, Observer {
            spinner.isVisible = false
            when (it) {
                is Resource.Success -> populateCandlesUI(it.data, true)
                is Resource.Failure -> {
                    it.data?.let { data -> populateCandlesUI(data, false) }
                    if (!it.hasBeenHandled && parentViewModel.boxesLiveData.value == false) parseError(it.e()!!)
                }
            }
        })
        parentViewModel.topLiveData.observe(viewLifecycleOwner, Observer { isTop ->
            when (val adapter = recyclerViewHome.adapter) {
                is HomeAdapter -> if (adapter.items.isNotEmpty()) {
                    val items = sortUnderlyings(adapter.items, isTop)
                    adapter.submitList(items)
                }
                is CandleAdapter -> if (adapter.items.isNotEmpty()) {
                    val items = sortCandleSticks(adapter.items, isTop)
                    adapter.submitList(items)
                }
            }
        })
        viewModel.indexLiveData.observe(viewLifecycleOwner, Observer {
            spinnerIndex.isVisible = false
            when (it) {
                is Resource.Success -> populateIndexUI(it.data, true)
                is Resource.Failure -> {
                    it.data?.let { data -> populateIndexUI(data, false) }
                    if (!it.hasBeenHandled) parseError(it.e()!!)
                }
            }
        })
        viewModel.productModelLiveData.observe(viewLifecycleOwner, Observer { productModel ->
            (viewModel.indexLiveData.value as? Resource.Success)?.data?.let {
                if (it.isNotEmpty() && it[0].id == productModel.id) {
                    setIndex(productModel.title, productModel.lastTraded, productModel.priceChangePct, productModel.priceDateTime)
                    viewModel.updateIndex(productModel)
                    return@Observer
                }
            }
            (recyclerViewHome.adapter as? HomeAdapter)?.let { adapter ->
                if (adapter.updateItem(productModel)) viewModel.updateUnderlying(productModel)
            }
        })
        viewModel.candleModelLiveData.observe(viewLifecycleOwner, Observer { candleModel ->
            viewModel.updateCandleStick(candleModel, type)

            populateCandleStickFromSocket(candleModel)
        })
    }

    private fun populateIndexUI(data: List<IndexModel>, subscribeToRtUpdates: Boolean) {
        if (data.isEmpty()) return

        setIndex(data[0].title, data[0].lastTraded, data[0].priceChangePct, data[0].date)

        if (parentViewModel.boxesLiveData.value == true) {
            cardView2.isVisible = true
        }
        cardView2.setOnClickListener {
            openIndex(data[0])
        }
        marketCardCandle.setOnClickListener {
            openIndex(data[0])
        }
        if (subscribeToRtUpdates) viewModel.subscribeToRtUpdates(data.map { it.id }, SubscriptionPrefix.PRODUCT)
    }

    private fun openIndex(indexModel: IndexModel) {
        (activity as? NavigationHost)?.openFragment(IndexDetailFragment.newInstance(indexModel.id, false, indexModel), null, addToBackStack = true)
    }

    private fun populateCandlesUI(data: Pair<Int?, List<CandleStickModel>>, subscribeToRtUpdates: Boolean) {
        val adapter = (recyclerViewHome.adapter as? CandleAdapter) ?: return

        val (indexId, items) = data

        if (items.isNotEmpty()) {
            headerCandle.isVisible = true

            var min = Util.nearestInteger(items.minBy { it.priceChangePct }!!.priceChangePct.times(100))
            if (min > 0) min = 0
            var max = Util.nearestInteger(items.maxBy { it.priceChangePct }!!.priceChangePct.times(100))
            if (max < 0) max = 0

            candlesMin = min
            candlesMax = max
            val zeroPercentPosition = setValuesOnHeader(min, max) * 25

            val other = indexId?.let { iid ->
                items.firstOrNull { it.productId == iid }?.let {
                    indexCandleStick = it

                    txtTitle.text = it.title
                    candleStickChart.setData(it, min, max)

                    grid.selectedPos = zeroPercentPosition.toFloat()
                    marketCardCandle.isVisible = true
                }
                items.filter { it.productId != iid }
            } ?: items

            val itemsSorted = sortCandleSticks(other, storage.getTop())
            adapter.zeroPercentPosition = zeroPercentPosition
            adapter.submitList(itemsSorted, min, max)

            if (subscribeToRtUpdates) viewModel.subscribeToRtUpdates(items.map { model -> model.productId }, SubscriptionPrefix.CANDLESTICK)
        } else {
            adapter.submitList(items, -2, 2)
            headerCandle.isVisible = false
        }
    }

    private fun populateCandleStickFromSocket(socketModel: CandleStickUpdateModel) {
        val adapter = (recyclerViewHome.adapter as? CandleAdapter) ?: return

        val isIndexUpdate = indexCandleStick?.productId == socketModel.id
        if (isIndexUpdate) {
            indexCandleStick?.updateFromSocketModel(socketModel)
        }
        val allCandleSticks = indexCandleStick?.let { adapter.items.plus(it) } ?: adapter.items

        var min = Util.nearestInteger(allCandleSticks.minBy { it.priceChangePct }!!.priceChangePct.times(100))
        if (min > 0) min = 0
        var max = Util.nearestInteger(allCandleSticks.maxBy { it.priceChangePct }!!.priceChangePct.times(100))
        if (max < 0) max = 0

        if (min != candlesMin || max != candlesMax) {
            candlesMin = min
            candlesMax = max

            val zeroPercentPosition = setValuesOnHeader(min, max) * 25
            adapter.zeroPercentPosition = zeroPercentPosition
            grid.selectedPos = zeroPercentPosition.toFloat()
        }
        if (isIndexUpdate) {
            indexCandleStick?.let {
                candleStickChart.setData(it, min, max)
            }
            if (min != adapter.min || max != adapter.max) {
                adapter.submitList(adapter.items, min, max)
            }
        } else {
            val items = adapter.items
            val oldModel = items.firstOrNull { it.productId == socketModel.id }
            if (oldModel?.isEqual(socketModel) == false) {
                oldModel.updateFromSocketModel(socketModel)

                val itemsSorted = if (storage.getTop()) {
                    sortCandleSticksByPerformance(items)
                } else {
                    items.toList()
                }
                adapter.submitList(itemsSorted, min, max, socketModel.id)
            }
        }
    }

    private fun setValuesOnHeader(minValue: Int, maxValue: Int): Int {
        val centerValue: Double = when {
            maxValue == 0 -> minValue / 2.0
            minValue == 0 -> maxValue / 2.0
            else -> 0.0
        }
        val centerLeft = (centerValue + minValue) / 2.0
        val centerRight = (centerValue + maxValue) / 2.0

        txtLeftPercent.text = "$minValue%"
        txtLeftPercent.setTextColor(ContextCompat.getColor(requireContext(), getNumberColor(minValue.toDouble())))

        txtRightPercent.text = "$maxValue%"
        txtRightPercent.setTextColor(ContextCompat.getColor(requireContext(), getNumberColor(maxValue.toDouble())))

        txtCenter.text = centerValue.format(1) + "%"
        txtCenter.setTextColor(ContextCompat.getColor(requireContext(), getNumberColor(centerValue)))

        txtCenterLeft.text = centerLeft.format(1) + "%"
        txtCenterLeft.setTextColor(ContextCompat.getColor(requireContext(), getNumberColor(centerLeft)))

        txtCenterRight.text = centerRight.format(1) + "%"
        txtCenterRight.setTextColor(ContextCompat.getColor(requireContext(), getNumberColor(centerRight)))

        val values = doubleArrayOf(minValue.toDouble(), centerLeft, centerValue, centerRight, maxValue.toDouble())
        val pos = values.binarySearch(0.0)
        return if (pos >= 0) pos else values.size / 2
    }

    private fun getNumberColor(v: Double): Int {
        return when {
            v > 0 -> R.color.blueGreen
            v < 0 -> R.color.rouge
            else -> R.color.black
        }
    }

    private fun populateUnderlyingUI(data: List<UnderlyingModel>) {
        (recyclerViewHome.adapter as? HomeAdapter)?.let { adapter ->
            val items = sortUnderlyings(data, storage.getTop())
            adapter.submitList(items)
        }
    }

    private fun sortUnderlyings(data: List<UnderlyingModel>, isTop: Boolean): List<UnderlyingModel> {
        return if (isTop) {
            data.sortedWith(compareByDescending<UnderlyingModel> { it.priceChangePct }.thenBy { it.title.toLowerCase() })
        } else {
            data.sortedBy { it.title.toLowerCase() }
        }
    }

    private fun sortCandleSticks(data: List<CandleStickModel>, isTop: Boolean): List<CandleStickModel> {
        return if (isTop) {
            sortCandleSticksByPerformance(data)
        } else {
            data.sortedBy { it.title.toLowerCase() }
        }
    }

    private fun sortCandleSticksByPerformance(data: List<CandleStickModel>): List<CandleStickModel> {
        return data.sortedWith(compareByDescending<CandleStickModel> { it.priceChangePct }.thenBy { it.title.toLowerCase() })
    }

    private fun setRecyclerAutomatically(isBoxes: Boolean): Boolean {
        if (isBoxes) {
            hidePercentsHeader()

            with(recyclerViewHome) {
                if (adapter !is HomeAdapter) {
                    layoutManager = GridLayoutManager(context, 4)
                    adapter = HomeAdapter(storage, object : HomeAdapter.OnItemClickListener {
                        override fun onClick(model: UnderlyingModel) {
                            (activity as? NavigationHost)?.openFragment(CompanyFragment.newInstance(model.id), null, true, R.style.FragStyle)
                        }
                    })
                    return true
                }
            }
        } else {
            setPercentsHeader()

            with(recyclerViewHome) {
                if (adapter !is CandleAdapter) {
                    layoutManager = LinearLayoutManager(context)
                    adapter = CandleAdapter(object : CandleAdapter.OnCandleClickListener {
                        override fun onClick(item: CandleStickModel) {
                            (activity as? NavigationHost)?.openFragment(CompanyFragment.newInstance(item.productId), null, true, R.style.FragStyle)
                        }
                    })
                    return true
                }
            }
        }
        return false
    }

    private fun hidePercentsHeader() {
        if (txtNameMarket.text.isNotEmpty()) cardView2.isVisible = true

        marketCardCandle.isVisible = false
        headerCandle.isVisible = false
    }

    private fun setPercentsHeader() {
        cardView2.isVisible = false
        if (txtTitle.text.isNotEmpty()) {
            marketCardCandle.isVisible = true
        }
    }

    private fun setIndex(name: String, lastTraded: Double?, priceChangePct: Double, date: Long?) {
        if (name.isNotEmpty()) {
            txtNameMarket.text = name
        }
        when {
            priceChangePct < 0 -> {
                (cardView2.background as GradientDrawable).setColor(ContextCompat.getColor(requireContext(), R.color.rouge))
                val textColor = ContextCompat.getColor(requireContext(), R.color.white)
                txtPercentHeader.setTextColor(textColor)
                txtNameMarket.setTextColor(textColor)
                txtDateTime.setTextColor(textColor)
                txtAsk1.setTextColor(textColor)
            }
            priceChangePct > 0 -> {
                (cardView2.background as GradientDrawable).setColor(ContextCompat.getColor(requireContext(), R.color.blueGreen))
                val textColor = ContextCompat.getColor(requireContext(), R.color.white)
                txtPercentHeader.setTextColor(textColor)
                txtPercentHeader.setTextColor(textColor)
                txtNameMarket.setTextColor(textColor)
                txtDateTime.setTextColor(textColor)
                txtAsk1.setTextColor(textColor)
            }
            else -> {
                (cardView2.background as GradientDrawable).setColor(ContextCompat.getColor(requireContext(), R.color.white))
                val textColor = ContextCompat.getColor(requireContext(), R.color.black)
                txtPercentHeader.setTextColor(textColor)
                txtPercentHeader.setTextColor(textColor)
                txtNameMarket.setTextColor(textColor)
                txtDateTime.setTextColor(textColor)
                txtAsk1.setTextColor(textColor)
            }
        }
        txtAsk1.text = lastTraded?.format(2)
        txtRatio.text = lastTraded?.format(2)

        txtProcent.text = priceChangePct.formatPercent(resources)
        txtProcent.setTextColor(ContextCompat.getColor(requireContext(), getNumberColor(priceChangePct)))

        txtPercentHeader.text = priceChangePct.formatPercent(resources)
        txtDateTime.text = date?.formatDate(DATE_FORMAT) ?: ""
    }


    override fun onResume() {
        super.onResume()

        doRequests(NetworkStateManager.isNetworkAvailable(requireContext()))
    }

    private fun doRequests(isOnline: Boolean) {
        if (isOnline) viewModel.resubscribeToRtUpdates()
        if (isOnline || isFirstStart) {
            spinnerIndex.isVisible = true
            spinner.isVisible = true

            viewModel.loadIndexAndCandlesSticks(type)
            viewModel.loadUnderlyings(type)
        }
    }

    override fun onOffline() {
        if ((parentViewModel.boxesLiveData.value == true && (recyclerViewHome.adapter as HomeAdapter).itemCount == 0 && txtPercentHeader?.text?.isEmpty() == true) ||
                (parentViewModel.boxesLiveData.value == false && (recyclerViewHome.adapter as CandleAdapter).itemCount == 0)) {
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
