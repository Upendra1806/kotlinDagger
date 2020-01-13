package com.juliusbaer.premarket.ui.alerts

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.jakewharton.rxbinding3.widget.textChanges
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.models.DetailWarrModel
import com.juliusbaer.premarket.models.ProductType
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.models.serverModels.AlertItemModel
import com.juliusbaer.premarket.models.serverModels.AlertSendModel
import com.juliusbaer.premarket.ui.ProgressDialog
import com.juliusbaer.premarket.ui.base.BaseActivity
import com.juliusbaer.premarket.ui.customViews.seekBar.RangeSeekBar
import com.juliusbaer.premarket.ui.fragments.extentions.format
import com.juliusbaer.premarket.ui.fragments.extentions.round
import com.juliusbaer.premarket.utils.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_underlying_alert.*
import kotlinx.android.synthetic.main.header_app.*
import kotlinx.android.synthetic.main.include_toolbar.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class UnderlyingAlertsActivity : BaseActivity(R.layout.activity_underlying_alert) {
    private var textChangesObservable: Disposable? = null
    private var list: ArrayList<AlertSendModel> = ArrayList()
    private val id by lazy { intent.getIntExtra(EXTRA_PRODUCT_ID, 0) }
    private val type by lazy { (intent.getSerializableExtra(EXTRA_TYPE) as? ProductType)?: ProductType.UNDERLYING }
    private var max: Double = 0.0
    private var currentPrice: Double = 0.0

    private val viewModel by viewModels<UnderlyingAlertsViewModel> { viewModelFactory }

    private var progressDialog: Dialog? = null

    private var precision: Int = Constants.PRECISION

    companion object {
        private const val EXTRA_PRODUCT_ID = "productId"
        private const val EXTRA_TYPE = "type"
        private const val EXTRA_PRECISION = "precision"
        private val EXPIRE_DATE_FORMAT = SimpleDateFormat(Constants.DATE_ONLY_FORMAT, Locale.US)

        fun newIntent(context: Context, productId: Int, type: ProductType, precision: Int? = null): Intent {
            return Intent(context, UnderlyingAlertsActivity::class.java)
                    .putExtra(EXTRA_PRODUCT_ID, productId)
                    .putExtra(EXTRA_TYPE, type).apply {
                        precision?.let { putExtra(EXTRA_PRECISION, it) }
                    }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        precision = intent.getIntExtra(EXTRA_PRECISION, Constants.PRECISION)

        toolbar.inflateMenu(R.menu.save_settings)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.apply -> {
                    setAlertClick()
                    true
                }
                else -> false
            }
        }
        toolbar.setNavigationIcon(R.drawable.icon_caret_left_white)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        when (type) {
            ProductType.WARRANT -> initWarrantLayout()
            ProductType.CURRENCY,
            ProductType.METAL -> newsLayout.isVisible = false
        }
        if (type != ProductType.WARRANT) {
            price.max = 10000000
        }
        initPriceFilter()

        observePriceInput()

        price.setOnMarkClickListener {
            price.progress = (currentPrice * price.max / max).toInt()
            setPriceInternal(currentPrice)
        }
        checkPrice.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                price.isVisible = true
                current_price_label.isVisible = true
                current_price_text_view.isVisible = true
            } else {
                price.isVisible = false
                current_price_label.isVisible = false
                current_price_text_view.isVisible = false
            }
            setVisibilityContainer(isChecked, null, txtPriceMax, null, etInputPrice)
        }
        imgBtnSetAlert.setOnClickListener { setAlertClick() }

        viewModel.productLiveData.observe(this, Observer {
            progressDialog?.hide()
            when (it) {
                is Resource.Success -> getUnderlyingResult(it.data)
                is Resource.Failure -> if (!it.hasBeenHandled) parseError(it.e()!!)
            }
        })
        viewModel.alertsLiveData.observe(this, Observer {
            when (it) {
                is Resource.Success -> getAlert(it.data)
                is Resource.Failure -> if (!it.hasBeenHandled) parseError(it.e()!!)
            }
            progressDialog?.hide()
        })
        viewModel.setAlertLiveData.observe(this, Observer {
            when (it) {
                is Resource.Success -> finish()
                is Resource.Failure -> if (!it.hasBeenHandled) parseError(it.e()!!, true)
            }
        })
        progressDialog = ProgressDialog.progressDialog(this).apply { show() }
        viewModel.loadProductAndAlerts(id, type)
    }

    private fun initPriceFilter() {
        if (UiUtils.isCompatWithO) {
            etInputPrice.filters = arrayOf(ValueFilterAPIOreo().apply { setDigits(precision) })
        } else {
            etInputPrice.filters = arrayOf(ValueFilter().apply { setDigits(precision) })
        }
    }

    private fun initWarrantLayout() {
        expiryLay.isVisible = true

        dateVolume.setTypeBage(RangeSeekBar.INT_TYPE)
        dateVolume.maxValue = 15f
        dateVolume.setRules(0f, 15f, 0f, 1)

        rangeSeekBarExpiry.leftSB?.isShowingHint = true
        rangeSeekBarExpiry.setOnRangeChangedListener(object : RangeSeekBar.OnRangeChangedListener {
            override fun onRangeChanged(view: RangeSeekBar, min: Float, max: Float, isFromUser: Boolean) {
                if (isFromUser) {
                    rangeSeekBarExpiry.setLeftProgressDescription(getString(R.string.n_days, min.toInt()))
                }
            }
        })
        checkExpiry.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                alertStartExpireDate.isVisible = true
                alertEndExpireDate.isVisible = true
            } else {
                alertStartExpireDate.isVisible = false
                alertEndExpireDate.isVisible = false
            }
            setVisibilityContainer(isChecked, null, null, rangeSeekBarExpiry, null)
        }
        checkstrikePrice.setOnCheckedChangeListener { _, isChecked ->
            setVisibilityContainer(isChecked, null, null, rangeSeekBarstrikePrice, null)
        }
        checkpriceDistance.setOnCheckedChangeListener { _, isChecked ->
            setVisibilityContainer(isChecked, null, null, rangeSeekBarpriceDistance, null)
        }
        checkVolume.setOnCheckedChangeListener { _, isChecked ->
            setVisibilityContainer(isChecked, txtVolumeInit, txtVolumeMax, dateVolume, null)
        }
    }

    private fun setExpireDaysUI(alertValue: Double) {
        rangeSeekBarExpiry.setValue(alertValue.toFloat())
        rangeSeekBarExpiry.setLeftProgressDescription(getString(R.string.n_days, alertValue.toInt()))
    }

    override fun onStop() {
        if (isFinishing) {
            progressDialog?.dismiss()
            textChangesObservable?.dispose()
        }
        super.onStop()
    }

    private fun setVisibilityContainer(isVisible: Boolean, txtInit: TextView?, txtMax: TextView?, seekBar: RangeSeekBar?, editText: EditText?) {
        editText?.isVisible = isVisible
        txtInit?.isVisible = isVisible
        txtMax?.isVisible = isVisible
        seekBar?.isVisible = isVisible
    }

    private fun getAlertModel(): List<AlertSendModel> {
        list.clear()
        if (checkPrice.isChecked) {
            list.add(AlertSendModel(0, Constants.PRICE_CRITERION, etInputPrice.text.toString()))
        }
        if (checkNews.isChecked) {
            list.add(AlertSendModel(0, "News", "1"))
        }
        if (checkVolume.isChecked) {
            list.add(AlertSendModel(0, Constants.VOLUME_CRITERION, dateVolume.currentRange[0].toDouble().format(2)))
        }
        if (checkExpiry.isChecked) {
            list.add(AlertSendModel(0, Constants.EXPIRY_CRITERION, rangeSeekBarExpiry.leftSB?.mHintText2Draw?.removeSuffix(" Days")))
        }
        return list
    }

    private fun setAlertClick() {
        val alertList = getAlertModel()
        if (alertList.isNotEmpty()) {
            viewModel.setAlert(id, alertList)
        } else {
            val msg = when (type) {
                ProductType.INDEX -> R.string.are_you_sure_dont_want_receive_alerts_by_index
                ProductType.UNDERLYING -> R.string.are_you_sure_dont_want_receive_alerts_by_underlying
                ProductType.WARRANT -> R.string.are_you_sure_dont_want_receive_alerts_by_warrant
                ProductType.METAL, ProductType.CURRENCY -> R.string.are_you_sure_dont_want_receive_alerts_by_pair
                else -> return
            }
            AlertDialog.Builder(this)
                    .setMessage(msg)
                    .setPositiveButton(R.string.yes) { _, _ ->
                        viewModel.setAlert(id, alertList)
                    }
                    .setNegativeButton(R.string.no, null)
                    .show()
        }
    }

    private fun getUnderlyingResult(product: AlertItem) {
        currentPrice = product.lastTraded ?: 0.0

        max = if (currentPrice > 0) currentPrice * 3 else 1.0

        current_price_text_view.text = currentPrice.format(precision)
        price.markProgress = (currentPrice * price.max / max ).toInt()
        price.progress = price.max

        setPriceInternal(max)

        price.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val priceSliderValue = (max * progress / price.max)
                if (fromUser) {
                    setPriceInternal(priceSliderValue)
                }
                current_price_text_view.isActivated = currentPrice.round(2) == priceSliderValue.round(2)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        txtPriceMax.text = max.format(precision)
        screenTitle.text = getString(R.string.set_alert_on, product.title)

        if (product is AlertItem.Warrant) {
            populateWarrantUI(product.model)
        }
    }

    private fun populateWarrantUI(detailWarrant: DetailWarrModel) {
        val expireEndDate = detailWarrant.exerciseDate!! * 1000
        val expireStartDate = Date().time
        var expirePeriod = Util.daysBetweenToTimeStamps(expireStartDate, expireEndDate)
        if (expirePeriod < 1) expirePeriod = 1

        rangeSeekBarExpiry.max = expirePeriod.toFloat()
        rangeSeekBarExpiry.maxValue = expirePeriod.toFloat()

        alertStartExpireDate.text = EXPIRE_DATE_FORMAT.format(expireStartDate)
        alertEndExpireDate.text = EXPIRE_DATE_FORMAT.format(Date(expireEndDate))
        setExpireDaysUI(expirePeriod.toDouble())
    }

    private fun getAlert(alert: List<AlertItemModel>) {
        alert.forEach {
            when (it.criterion) {
                Constants.PRICE_CRITERION -> {
                    checkPrice.isChecked = true

                    val priceReceived = it.value!!

                    price.progress = (priceReceived * price.max / max).toInt()

                    setPriceInternal(priceReceived)
                }
                Constants.VOLUME_CRITERION -> {
                    checkVolume.isChecked = true
                    dateVolume.leftSB?.currPercent = it.minValue!!.toFloat() / 100
                    dateVolume.rightSB?.currPercent = it.maxValue!!.toFloat() / 100
                }
                Constants.EXPIRY_CRITERION -> {
                    checkExpiry.isChecked = true

                    val alertValue = it.value ?: 0.0

                    setExpireDaysUI(alertValue)
                }
                Constants.NEWS -> {
                    checkNews.isChecked = true
                }
            }
        }
    }

    private fun setPriceInternal(price: Double) {
        textChangesObservable?.dispose()
        etInputPrice.setText(price.format(precision))
        observePriceInput()
    }

    private fun observePriceInput() {
        textChangesObservable = etInputPrice.textChanges()
                .debounce(1000, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .map { charSequence -> charSequence.toString() }.subscribe {
                    try {
                        val inputPrice = it.toFloat()
                        val newPriceProgress = if (inputPrice > max) {
                            price.max
                        } else {
                            (inputPrice / max * price.max).roundToInt()
                        }
                        price.progress = newPriceProgress
                    } catch (e: NumberFormatException) {

                    }
                }
    }
}
