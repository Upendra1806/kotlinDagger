package com.juliusbaer.premarket.helpers.chart


import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.utils.Utils
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.models.CandleStickModel.CandleData
import com.juliusbaer.premarket.utils.Constants

class PerformanceCandleChart @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CandleStickChart(context,attrs,defStyleAttr){

    private var period: ChartInterval? = null
    private var xStart: Long? = null
    private var intervalParam: Int? = null
    var precision: Int = Constants.PRECISION
        set(value) {
            field = value
            (axisLeft.valueFormatter as? YAxisValueFormatter)?.precision = value
        }

    init {
        setNoDataText("")

        setScaleEnabled(false)

        description.isEnabled = false
        legend.isEnabled = false
        axisRight.isEnabled = false

        axisLeft.apply {
            textColor = R.color.warmGreyTwo
            textSize = 9f
            typeface = ResourcesCompat.getFont(context, R.font.verlag_light)
            valueFormatter = YAxisValueFormatter(precision)
        }
        setXAxisRenderer(XAxisRendererLastLabelFix(viewPortHandler, xAxis, getTransformer(YAxis.AxisDependency.LEFT)))

        xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            textColor = R.color.warmGreyTwo
            textSize = 9f
            typeface = ResourcesCompat.getFont(context, R.font.verlag_light)
            setDrawGridLines(false)
            setAvoidFirstLastClipping(true)
            valueFormatter = HourAxisValueFormatter()
        }
    }

    fun updateDataChart(price: Double, time: Long) {
        if (data != null && data.dataSetCount > 0 && period == ChartInterval.INTRADAY) {
            val set1 = data.getDataSetByIndex(0) as LineDataSet

            val dataSet = set1.values.plus(Entry(time.toFloat(), price.toFloat()))
            xStart?.let {
                setLabels(Pair(it, time), period!!, intervalParam)
            }
            set1.values = dataSet

            data.notifyDataChanged()
            notifyDataSetChanged()
        }
    }

    fun setData(entries: List<CandleData>, period: ChartInterval, intervalParam: Int? = null) {
        this.period = period
        this.intervalParam = intervalParam

        if (entries.isNotEmpty()) {
            val dataSet = entries.map { CandleEntry(it.x.toFloat(), it.shadowHigh,it.shadowLow,it.open,it.close) }

            xAxis.apply {
                val xRange = entries.firstOrNull()?.let {
                    xStart = it.x
                    Pair(it.x, entries.last().x)
                }
                when (period) {
                    ChartInterval.INTRADAY -> setLabels(xRange, period, intervalParam)
                    else -> setLabels(xRange, period)
                }
            }
            if (data != null && data.dataSetCount > 0) {
                val set1 = data.getDataSetByIndex(0) as CandleDataSet
                set1.values = dataSet

                data.notifyDataChanged()
                notifyDataSetChanged()
            } else {
                val candleDataSet = CandleDataSet(dataSet, "Performance").apply {
                    /*setDrawMarkers(false)
                    axisDependency = YAxis.AxisDependency.LEFT
                    setColors(ContextCompat.getColor(context, R.color.tealBlue))
                    color = ContextCompat.getColor(context, R.color.tealBlue)
                    color = ContextCompat.getColor(context, R.color.persianBlue)
                    setDrawBorders(true)
                    valueTextColor = Color.TRANSPARENT*/


                    setDrawIcons(false);
                    axisDependency = YAxis.AxisDependency.LEFT;
                    shadowColor = Color.DKGRAY;
                    shadowWidth = 0.7f;
                    decreasingColor = Color.RED;
                    decreasingPaintStyle = Paint.Style.FILL;
                    increasingColor = Color.rgb(122, 242, 84);
                    increasingPaintStyle = Paint.Style.STROKE;
                    neutralColor = Color.BLUE;

                }

                data = CandleData(candleDataSet)
            }
        } else {
            setPaint(Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = ContextCompat.getColor(context, R.color.warmGreyB)
                textAlign =  Paint.Align.CENTER
                textSize = Utils.convertDpToPixel(14f)
                typeface = ResourcesCompat.getFont(context, R.font.verlag_light)
            }, Chart.PAINT_INFO)
            setNoDataText(resources.getString(R.string.no_data))
        }
        invalidate()
    }

    private fun setLabels(xRange: Pair<Long, Long>?, period: ChartInterval, intervalParam: Int? = null) {
        val interval = if (intervalParam != null && intervalParam > 0) intervalParam else period.interval
        val labelValues = mutableListOf<Long>()
        xRange?.let {
            val end = xRange.second
            var time = xRange.first
            do {
                labelValues.add(time)
                time += interval
            } while (time < end)
        }
        xAxis.setLabelCount(labelValues.size, true)

        (rendererXAxis as XAxisRendererLastLabelFix).apply {
            this.format = if (period == ChartInterval.INTRADAY) XAxisRendererLastLabelFix.FORMAT_INTRADAY else XAxisRendererLastLabelFix.FORMAT_OTHER
            this.labelValues = labelValues
        }
    }


}
