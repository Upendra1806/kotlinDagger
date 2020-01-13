package com.juliusbaer.premarket.ui.filter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Selection
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.models.FilterModel
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.models.serverModels.ExtremeWarrantModel
import com.juliusbaer.premarket.ui.base.*
import com.juliusbaer.premarket.ui.customViews.seekBar.RangeSeekBar
import com.juliusbaer.premarket.ui.fragments.extentions.format
import com.juliusbaer.premarket.ui.fragments.extentions.round
import com.juliusbaer.premarket.ui.warrants.WarrantsFragment
import com.juliusbaer.premarket.utils.ArrayAdapterNormalized
import com.juliusbaer.premarket.utils.Constants
import com.juliusbaer.premarket.utils.Util
import com.juliusbaer.premarket.utils.getNormalizedString
import kotlinx.android.synthetic.main.fragment_filter.*
import kotlinx.android.synthetic.main.header_app.*
import kotlinx.android.synthetic.main.include_toolbar.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


class FilterFragment : BaseNFragment(R.layout.fragment_filter), HasOfflinePlaceHolder, NavigationChild {
    override val titleResId: Int
        get() = R.string.filter

    private var maturityStartDateTimeStamp: Long = 0L

    private var maturityEndDateTimeStamp: Long = 0L

    private var strikePriceMinValue: Double = 0.0
    private var strikePriceMaxValue: Double = 0.0
    private var productLastTraded: Double? = null

    private var productId: Int? = null
    private val viewModel by viewModels<FilterViewModel> { viewModelFactory }

    private var hasData: Boolean = false
    private var extremeAllWarrantsValues: ExtremeWarrantModel? = null
    private var loadedProductId: Int? = null

    private var filter: FilterModel? = null

    private val dateFormat by lazy { SimpleDateFormat(Constants.DATE_ONLY_FORMAT, Locale.getDefault()) }
    private var validAutoCompleteConstraint: String? = null

    companion object {
        private const val ARG_PRODUCT_ID = "productId"
        private const val ARG_FILTER = "filter"

        const val EXTRA_FILTER = "filter"

        fun newInstance(underlyingId: Int? = null, filterModel: FilterModel? = null): FilterFragment {
            return FilterFragment().apply {
                arguments = Bundle().apply {
                    underlyingId?.let { putInt(ARG_PRODUCT_ID, it) }
                    filterModel?.let { putParcelable(ARG_FILTER, it) }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        screenTitle.setText(R.string.filter)

        dateVolume.markList = listOf(Pair(0f, "0"), Pair(10f, "100K"), Pair(50f, "500K"), Pair(100f, "1M"))
        dateVolume.leftSB?.isShowingHint = true
        dateVolume.setProgressDescription(getString(R.string.show_all))
        dateVolume.setMarkClickListener { progress, label ->
            dateVolume.leftSB?.currPercent = progress / dateVolume.max
            val text = if (progress == 0f) getString(R.string.show_all) else label
            text?.let { dateVolume.setProgressDescription(it) }

            dateVolume.invalidate()
        }
        strikePrice.rightSB?.currPercent = 1f
        strikePrice.setMarkClickListener { progress, _ ->
            setCurrentStrikePrice(progress)
        }
        strikePrice.setOnRangeChangedListener(object : RangeSeekBar.OnRangeChangedListener {
            override fun onRangeChanged(view: RangeSeekBar, min: Float, max: Float, isFromUser: Boolean) {
                showStrikePriceHints(min, max, productLastTraded)
            }
        })
        underlyingPriceLayout.setOnClickListener {
            val strikePriceDiff = strikePriceMaxValue - strikePriceMinValue
            setCurrentStrikePrice((((productLastTraded ?: 0.0) - strikePriceMinValue) * strikePrice.max / strikePriceDiff).toFloat())
        }
        maturityValue.leftSB?.isShowingHint = true
        maturityValue.rightSB?.isShowingHint = true
        maturityValue.rightSB?.currPercent = 1f
        maturityValue.setOnRangeChangedListener(object : RangeSeekBar.OnRangeChangedListener {
            override fun onRangeChanged(view: RangeSeekBar, min: Float, max: Float, isFromUser: Boolean) {
                showMaturityHints(min, max)
            }
        })

        val id = arguments?.getInt(ARG_PRODUCT_ID, 0) ?: 0
        productId = if (id > 0) id else null

        filter = arguments?.getParcelable(ARG_FILTER)

        if (id > 0) {
            cardViewProduct.isVisible = true
            underlyingSearchLayout.isVisible = false
        } else {
            cardViewProduct.isVisible = false
            underlyingSearchLayout.isVisible = true

            setupAllWarrantsUI()
        }

        cardViewProduct.setOnClickListener { (activity as? NavigationHost)?.openFragment(WarrantsFragment(), WarrantsFragment.TAG, true) }
        btnCall.setOnClickListener {
            btnCall.isActivated = !btnCall.isActivated
            if (btnCall.isActivated) btnPut.isActivated = false
        }

        btnPut.setOnClickListener {
            btnPut.isActivated = !btnPut.isActivated
            if (btnPut.isActivated) btnCall.isActivated = false
        }
        toolbar.inflateMenu(R.menu.save_settings)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.apply -> {
                    applyFilter()
                    true
                }
                else -> false
            }
        }
        toolbar.setNavigationIcon(R.drawable.ic_close_white)
        toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
        btnFilterStart.setOnClickListener {
            applyFilter()
        }
        dateVolume.setOnRangeChangedListener(object : RangeSeekBar.OnRangeChangedListener {
            override fun onRangeChanged(view: RangeSeekBar, min: Float, max: Float, isFromUser: Boolean) {
                showVolumeHints(min)
            }
        })
        autocomplete.setFilterCompleteListener { count ->
            if (count == 1 && autocomplete.adapter.getItemId(0) == -1L) {
                setAutoCompleteText(validAutoCompleteConstraint)
            } else {
                validAutoCompleteConstraint = (autocomplete.adapter as SearchAdapter).constraint.toString()
            }
        }
    }

    private fun setAutoCompleteText(oldText: String?) {
        autocomplete.clearComposingText()
        autocomplete.setText(oldText, false)
        val spannable = autocomplete.text
        Selection.setSelection(spannable, spannable.length)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.productWarrantValuesLiveData.observe(viewLifecycleOwner, Observer {
            progressDialog.hide()
            when (it) {
                is Resource.Success -> {
                    hasData = true

                    val extremeValues = it.data

                    filter?.let(::populateFilter)

                    if (extremeValues.warrantsExist == true) {
                        populateExtremeWarrantValues(extremeValues)
                    } else {
                        autocomplete.setText("")
                        extremeAllWarrantsValues?.let { populateExtremeWarrantValues(it) }
                        AlertDialog.Builder(requireContext())
                                .setMessage(R.string.no_warrants_for_selected_equity)
                                .setPositiveButton(R.string.ok, null)
                                .show()
                    }
                }
                is Resource.Failure -> if (!it.hasBeenHandled) parseError(it.e()!!)
            }
        })
        viewModel.allWarrantValuesLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    hasData = true

                    val extremeValues = it.data
                    extremeAllWarrantsValues = extremeValues

                    filter?.let { filter ->
                        if (filter.underlyingId == null) {
                            populateFilter(filter)
                            populateExtremeWarrantValues(extremeValues)
                            progressDialog.hide()
                        }
                    } ?:run {
                        populateExtremeWarrantValues(extremeValues)
                        progressDialog.hide()
                    }
                }
                is Resource.Failure -> {
                    progressDialog.hide()
                    if (!it.hasBeenHandled) parseError(it.e()!!)
                }
            }
        })
        viewModel.underlyingsLiveData.observe(this, Observer {
            when (it) {
                is Resource.Success -> populateUnderlyingsUI(it.data)
                is Resource.Failure -> if (!it.hasBeenHandled) parseError(it.e()!!)
            }
        })
        doRequests()
    }

    private fun setCurrentStrikePrice(percent: Float) {
        strikePrice.leftSB?.currPercent = percent / strikePrice.max
        strikePrice.rightSB?.currPercent = percent / strikePrice.max
        showStrikePriceHints(percent, percent, productLastTraded)
    }

    private fun applyFilter() {
        val strikePriceDiff = strikePriceMaxValue - strikePriceMinValue
        val array = mutableListOf<Int>()
        if (switchWarrants.isChecked) {
            array.add(Constants.WARRANTS_CATEGORY_WARRANTS)
        }
        if (switchKnock.isChecked) {
            array.add(Constants.WARRANTS_CATEGORY_KNOCK_OUT)
        }
        if (switchRange.isChecked) {
            array.add(Constants.WARRANTS_CATEGORY_RANGE)
        }
        val maturityDiff = maturityEndDateTimeStamp - maturityStartDateTimeStamp
        val filter = FilterModel(
                productId ?: loadedProductId,
                if (productId == null) findAutoCompleteItemByTitle(autocomplete.text.toString())?.title else null,
                getContractOption(),
                maturityStartDateTimeStamp + (maturityDiff * (maturityValue.leftSB?.currPercent
                        ?: 0f)).toLong(),
                maturityStartDateTimeStamp + (maturityDiff * (maturityValue.rightSB?.currPercent
                        ?: 1f)).toLong(),
                (strikePriceDiff * strikePrice.leftSB!!.currPercent + strikePriceMinValue).round(2),
                (strikePriceDiff * strikePrice.rightSB!!.currPercent + strikePriceMinValue).round(2),
                tradedVolume = ((dateVolume.leftSB?.currPercent ?: 0f) * 1000000).toInt(),
                category = array)

        activity?.setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(EXTRA_FILTER, filter)
        })
        activity?.finish()
    }

    private fun setupAllWarrantsUI() {
        autocomplete.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                autocomplete.showDropDown()
            } else {
                if (autocomplete.text.isNotBlank()) {
                    val item = findAutoCompleteItemByTitle(autocomplete.text.toString())
                    if (item != null) {
                        autocomplete.setText(item.title)
                        viewModel.findExtremeWarrantValues(item.id)
                    } else {
                        autocomplete.setText("")
                    }
                }
                if (autocomplete.text.isBlank() && loadedProductId != null) {
                    extremeAllWarrantsValues?.let {
                        viewModel.cancelExtremeWarrantsRequest()
                        populateExtremeWarrantValues(it)
                    }
                }
            }
        }
    }

    private fun findAutoCompleteItemByTitle(title: String): FilterSearchModel? {
        (autocomplete.adapter as? SearchAdapter)?.let { adapter ->
            val searchTitle = title.getNormalizedString().toLowerCase()
            for (i in 0 until adapter.count) {
                val item = adapter.getItem(i)!!
                if (item.title.getNormalizedString().toLowerCase() == searchTitle) {
                    return item
                }
            }
        }
        return null
    }

    override fun onOnline() {
        doRequests()
    }

    override fun onOffline() {
        if (!hasData) {
            super.onOffline()
        }
    }

    private fun doRequests() {
        progressDialog.show()

        if (productId != null) {
            viewModel.loadProductWarrantValues(productId!!)
        } else {
            viewModel.loadAllWarrantValues(filter?.underlyingId)
            viewModel.loadUnderlyings()
        }
    }

    private fun showVolumeHints(min: Float) {
        val value = min.toInt() / 10
        when {
            value < 1 -> {
                dateVolume.setProgressDescription(getString(R.string.show_all))
                dateVolume.leftSB?.currPercent = 0f
            }
            value >= 1 && (min.toInt() / 10) < 5 -> {
                dateVolume.setProgressDescription("100k")
                dateVolume.leftSB?.currPercent = 0.10f
            }
            value > 3 && (min.toInt() / 10) > 8 -> {
                dateVolume.setProgressDescription("1M")
                dateVolume.leftSB?.currPercent = 1f
            }
            else -> {
                dateVolume.setProgressDescription("500k")
                dateVolume.leftSB?.currPercent = 0.50f
            }
        }
    }

    private fun getContractOption(): String? {
        return when {
            btnCall.isActivated -> "Call"
            btnPut.isActivated -> "Put"
            else -> null
        }
    }

    private fun populateExtremeWarrantValues(model: ExtremeWarrantModel) {
        loadedProductId = model.productId

        maturityStartDateTimeStamp = model.maturityStartDate ?: 0
        maturityEndDateTimeStamp = model.maturityEndDate ?: 0

        strikePriceMinValue = model.strikePriceMin ?: 0.0
        strikePriceMaxValue = model.strikePriceMax ?: 0.0
        productLastTraded = model.productLastTraded

        val strikePriceDiff = strikePriceMaxValue - strikePriceMinValue

        if (model.productId != null && model.productLastTraded != null) {
            underlyingPrice.text = model.productLastTraded.format(2)
            underlyingPriceLayout.isVisible = true

            if (model.productLastTraded in strikePriceMinValue..strikePriceMaxValue) {
                strikePrice.markList = listOf(Pair(((model.productLastTraded - strikePriceMinValue) * strikePrice.max / strikePriceDiff).toFloat(), null))
            } else {
                strikePrice.markList = emptyList()
            }
            strikePrice.setProgressHintMode(RangeSeekBar.HINT_MODE_ALWAYS_SHOW)
            strikePrice.leftSB?.isShowingHint = true
            strikePrice.rightSB?.isShowingHint = true
        } else {
            strikePrice.markList = emptyList()
            underlyingPriceLayout.isVisible = false

            strikePrice.setProgressHintMode(RangeSeekBar.HINT_MODE_ALWAYS_HIDE)
            strikePrice.leftSB?.isShowingHint = false
            strikePrice.rightSB?.isShowingHint = false
        }
        if (filter != null) {
            populateFilterRanges(filter!!, model)
            filter = null
        } else {
            populateFilterByExtremeValues(model)
        }
    }

    private fun populateFilter(filter: FilterModel) {
        autocomplete.setText(filter.underlyingTitle)
        btnCall.isActivated = filter.contractOption == "Call"
        btnPut.isActivated = filter.contractOption == "Put"
        dateVolume.leftSB?.currPercent = (filter.tradedVolume ?: 0) / 1000000f
        showVolumeHints(dateVolume.leftSB?.currPercent!! * 100)
        dateVolume.invalidate()

        switchWarrants.isChecked = filter.category?.contains(Constants.WARRANTS_CATEGORY_WARRANTS) == true
        switchKnock.isChecked = filter.category?.contains(Constants.WARRANTS_CATEGORY_KNOCK_OUT) == true
        switchRange.isChecked = filter.category?.contains(Constants.WARRANTS_CATEGORY_RANGE) == true
    }

    private fun populateFilterRanges(filter: FilterModel, model: ExtremeWarrantModel) {
        val maturityDiff = (maturityEndDateTimeStamp - maturityStartDateTimeStamp).toFloat()
        if (maturityDiff > 0) {
            maturityValue.leftSB?.currPercent = ((filter.maturityStartDate
                    ?: 0L) - maturityStartDateTimeStamp) / maturityDiff
            maturityValue.rightSB?.currPercent = ((filter.maturityEndDate
                    ?: 0L) - maturityStartDateTimeStamp) / maturityDiff
        } else {
            maturityValue.leftSB?.currPercent = 0f
            maturityValue.rightSB?.currPercent = 1f
        }
        showMaturityHints(
                maturityValue.leftSB?.currPercent!! * 100,
                maturityValue.rightSB?.currPercent!! * 100)

        val strikePriceDiff = strikePriceMaxValue - strikePriceMinValue
        if (strikePriceDiff > 0) {
            strikePrice.leftSB?.currPercent = (((filter.strikePriceMin
                    ?: 0.0) - strikePriceMinValue) / strikePriceDiff).toFloat()
            strikePrice.rightSB?.currPercent = (((filter.strikePriceMax
                    ?: 0.0) - strikePriceMinValue) / strikePriceDiff).toFloat()
        } else {
            strikePrice.leftSB?.currPercent = 0f
            strikePrice.rightSB?.currPercent = 1f
        }

        showStrikePriceHints(strikePrice.leftSB?.currPercent!! * 100,
                strikePrice.rightSB?.currPercent!! * 100,
                model.productLastTraded)
    }

    private fun populateFilterByExtremeValues(model: ExtremeWarrantModel) {
        showMaturityHints(0f, 100f)

        strikePrice.leftSB?.currPercent = 0f
        strikePrice.rightSB?.currPercent = 1f
        showStrikePriceHints(0f, strikePrice.max, model.productLastTraded)

        switchWarrants.isChecked = model.categories.contains(Constants.WARRANTS_CATEGORY_WARRANTS)
        switchKnock.isChecked = model.categories.contains(Constants.WARRANTS_CATEGORY_KNOCK_OUT)
        switchRange.isChecked = model.categories.contains(Constants.WARRANTS_CATEGORY_RANGE)
    }

    private fun showStrikePriceHints(min: Float, max: Float, lastTraded: Double?) {
        val strikePriceDiff = strikePriceMaxValue - strikePriceMinValue

        val strikeSliderLeft = (strikePriceDiff * min / strikePrice.max) + strikePriceMinValue
        val strikeSliderRight = (strikePriceDiff * max / strikePrice.max) + strikePriceMinValue

        if (lastTraded != null) {
            if (loadedProductId != null) {
                strikePrice.setLeftProgressDescription((strikeSliderLeft * 100 / lastTraded).roundToInt().toString() + "%")
                strikePrice.setRightProgressDescription((strikeSliderRight * 100 / lastTraded).roundToInt().toString() + "%")
            }
            val productLastTradedRound = lastTraded.round(2)
            underlyingPrice.isActivated = strikeSliderLeft.round(2) == productLastTradedRound || strikeSliderRight == productLastTradedRound
        } else {
            underlyingPrice.isActivated = false
        }

        strikePrice.leftSB?.labelText2Draw = strikeSliderLeft.format(2)
        strikePrice.rightSB?.labelText2Draw = strikeSliderRight.format(2)
        strikePrice.invalidate()
    }

    private fun showMaturityHints(min: Float, max: Float) {
        val maturityDiff = maturityEndDateTimeStamp - maturityStartDateTimeStamp
        val maturitySliderLeft = (maturityDiff * min / 100f).toLong() + maturityStartDateTimeStamp
        val maturitySliderRight = (maturityDiff * max / 100f).toLong() + maturityStartDateTimeStamp
        maturityValue.setLeftProgressDescription(dateFormat.format(Date(maturitySliderLeft * 1000)))
        maturityValue.setRightProgressDescription(dateFormat.format(Date(maturitySliderRight * 1000)))

        val monthRange = if (maturityStartDateTimeStamp != 0L && maturityEndDateTimeStamp != 0L) {
            Util.monthBetweenToTimeStamps(maturityStartDateTimeStamp, maturityEndDateTimeStamp)
        } else {
            0
        }
        var monthsFromNow = Util.monthBetweenToTimeStamps(System.currentTimeMillis() / 1000, maturityStartDateTimeStamp)
        if (monthsFromNow < 0) monthsFromNow = 0

        maturityValue.leftSB?.labelText2Draw = getString(R.string.maturity_month_format, (monthRange * min / 100f).toInt() + monthsFromNow)
        maturityValue.rightSB?.labelText2Draw = getString(R.string.maturity_month_format, (monthRange * max / 100f).toInt() + monthsFromNow)
        maturityValue.invalidate()
    }

    private fun populateUnderlyingsUI(list: List<FilterSearchModel>) {
        autocomplete.setAdapter(SearchAdapter(requireContext(),
                android.R.layout.simple_dropdown_item_1line, list))

        autocomplete.setOnClickListener {
            //case when dropdown was hidden by clicking back button on device and then user click on empty but still focused input field
            if (!autocomplete.isPopupShowing && autocomplete.hasFocus()) {
                autocomplete.showDropDown()
            }
        }
        autocomplete.setOnItemClickListener { _, _, _, id ->
            autocomplete.clearFocus()
            //viewModel.getExtremeWarrantValues(id.toInt())
            (activity as? BaseActivity)?.hideSoftKeyboard()
        }
    }

    private class SearchAdapter(context: Context, resource: Int, objects: List<FilterSearchModel>) : ArrayAdapterNormalized<FilterSearchModel>(context, resource, objects) {
        override fun getItemId(position: Int): Long {
            return getItem(position)!!.id.toLong()
        }

        override val emptyItem = FilterSearchModel(-1, context.getString(R.string.no_results))

        override fun isEnabled(position: Int): Boolean {
            return getItem(position)!!.id > 0
        }
    }
}
