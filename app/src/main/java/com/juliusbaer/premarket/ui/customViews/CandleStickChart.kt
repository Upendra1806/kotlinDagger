package com.juliusbaer.premarket.ui.customViews

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.models.serverModels.CandleStickModel
import kotlinx.android.synthetic.main.candle_stick_card.view.*
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

class CandleStickChart : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    var max: Int = 0
    var min: Int = 0
    var candleStickModel: CandleStickModel? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.candle_stick_card, this, true)
    }

    fun setData(candleStickModel: CandleStickModel, min: Int, max: Int) {
        this.min = min
        this.max = max
        this.candleStickModel = candleStickModel

        setIncreasingColor(candleStickModel)
        setColorProgress(candleStickModel.priceChangePct)

        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val w = measuredWidth
        candleStickModel?.let { candleStickModel ->
            val highLowStart = value(w, candleStickModel.low)
            val highLowWidth = (value(w, candleStickModel.high) - highLowStart).toInt()
            highLow.measure(MeasureSpec.makeMeasureSpec(highLowWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(highLow.measuredHeight, MeasureSpec.EXACTLY))
            highLow.translationX = highLowStart.toFloat()

            val openStart = value(w, min(candleStickModel.last, candleStickModel.open))
            val openWidth = (value(w, max(candleStickModel.last, candleStickModel.open)) - openStart).toInt()
            imgOpen.measure(MeasureSpec.makeMeasureSpec(openWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(imgOpen.measuredHeight, MeasureSpec.EXACTLY))
            imgOpen.translationX = openStart.toFloat()

            imgLast.translationX = (value(w, candleStickModel.priceChangePct * 100) - imgLast.measuredWidth / 2).toFloat()
        } ?: run {
            imgLast.translationX = (value(w, 0.0) - imgLast.measuredWidth / 2).toFloat()
        }
    }

    private fun value(width: Int, percent: Double): Double {
        return if (max == 0 && min == 0) {
            width / 2.0
        } else if (max == 0) {
            width * (1 - percent / min)
        } else if (min == 0) {
            width * percent / max
        } else {
            val divider = if (percent > 0) max else min.absoluteValue
            width / 2.0 + width / 2.0 * percent / divider
        }
    }

    private fun setColorProgress(priceChangePct: Double) {
        val color = ContextCompat.getColor(context, if (priceChangePct < 0) R.color.rouge else R.color.blueGreen)

        imgOpen.setBackgroundColor(color)
        imgLast.setBackgroundColor(color)
    }

    private fun setIncreasingColor(candleStickModel: CandleStickModel) {
        when {
            candleStickModel.increasing -> imgLast.setBackgroundColor(ContextCompat.getColor(context, R.color.blueGreen))
            !candleStickModel.increasing and (candleStickModel.priceChangePct > 0) -> imgLast.setBackgroundColor(ContextCompat.getColor(context, R.color.blueGreen))
            !candleStickModel.increasing and (candleStickModel.priceChangePct < 0) -> imgLast.setBackgroundColor(ContextCompat.getColor(context, R.color.rouge))
        }
    }
}