package com.juliusbaer.premarket.helpers.chart

import android.graphics.Canvas
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.renderer.XAxisRenderer
import com.github.mikephil.charting.utils.*
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.util.*
import kotlin.math.roundToInt

class XAxisRendererLastLabelFix(viewPortHandler: ViewPortHandler, xAxis: XAxis, trans: Transformer): XAxisRenderer(viewPortHandler, xAxis, trans) {
    companion object {
        private val systemZoneId = ZoneId.systemDefault()
        const val FORMAT_INTRADAY = "H:mm"
        const val FORMAT_OTHER = "MMM,dd"
    }

    var labelValues: List<Long>? = null

    var format: String = ""
        set(value) {
            if (field != value) {
                field = value
                formatter = DateTimeFormatter.ofPattern(value, Locale.ENGLISH)
                longestLabel = LocalDateTime.ofInstant(Instant.ofEpochSecond(0), systemZoneId).format(formatter)
            }
        }

    private var formatter: DateTimeFormatter? = null
    private var longestLabel: String = ""

    override fun drawLabels(c: Canvas?, pos: Float, anchor: MPPointF?) {
        val labelRotationAngleDegrees = mXAxis.labelRotationAngle
        val centeringEnabled = mXAxis.isCenterAxisLabelsEnabled

        val positions = FloatArray(mXAxis.mEntryCount * 2)

        for (i in positions.indices step 2) {

            // only fill x values
            if (centeringEnabled) {
                positions[i] = mXAxis.mCenteredEntries[i / 2]
            } else {
                positions[i] = mXAxis.mEntries[i / 2]
            }
        }

        mTrans.pointValuesToPixel(positions)

        //special case with rendering fixed labels (independent from data values)
        if (labelValues?.isNotEmpty() == true && positions.isNotEmpty()) {
            var x = positions[0]
            val end = mViewPortHandler.chartWidth
            val step = (end - x) / labelValues!!.size

            for (i in labelValues!!.indices) {
                if (mViewPortHandler.isInBoundsX(x)) {

                    val label = LocalDateTime.ofInstant(Instant.ofEpochSecond(labelValues!![i]), systemZoneId).format(formatter)

                    if (mXAxis.isAvoidFirstLastClippingEnabled) {

                        // avoid clipping of the last
                        if (i == labelValues!!.size - 1) {
                            val width = Utils.calcTextWidth(mAxisLabelPaint, label).toFloat()

                            if (width > mViewPortHandler.offsetRight() * 2 && x + width > mViewPortHandler.chartWidth)
                                x -= width / 2

                            // avoid clipping of the first
                        } else if (i == 0) {

                            val width = Utils.calcTextWidth(mAxisLabelPaint, label).toFloat()
                            x += width / 2
                        }
                    }

                    drawLabel(c, label, x, pos, anchor, labelRotationAngleDegrees)
                }
                x += step
            }
        } else {
            //custom implementation for fixing bug in native implementation (v3.1.0) of clipping last label
            for (i in positions.indices step 2) {
                var x = positions[i]

                if (mViewPortHandler.isInBoundsX(x)) {

                    val label = mXAxis.valueFormatter.getAxisLabel(mXAxis.mEntries[i / 2], mXAxis)

                    if (mXAxis.isAvoidFirstLastClippingEnabled) {

                        // avoid clipping of the last
                        if (i == 2 * (mXAxis.mEntryCount - 1) && mXAxis.mEntryCount > 1) {
                            val width = Utils.calcTextWidth(mAxisLabelPaint, label).toFloat()

                            if (width > mViewPortHandler.offsetRight() * 2 && x + width > mViewPortHandler.chartWidth)
                                x -= width / 2

                            // avoid clipping of the first
                        } else if (i == 0) {

                            val width = Utils.calcTextWidth(mAxisLabelPaint, label).toFloat()
                            x += width / 2
                        }
                    }

                    drawLabel(c, label, x, pos, anchor, labelRotationAngleDegrees)
                }
            }
        }
    }

    override fun computeSize() {
        if (labelValues?.isNotEmpty() == true) {
            mAxisLabelPaint.typeface = mXAxis.typeface
            mAxisLabelPaint.textSize = mXAxis.textSize

            val labelSize = Utils.calcTextSize(mAxisLabelPaint, longestLabel)

            val labelWidth = labelSize.width
            val labelHeight = Utils.calcTextHeight(mAxisLabelPaint, "Q").toFloat()

            val labelRotatedSize = Utils.getSizeOfRotatedRectangleByDegrees(
                    labelWidth,
                    labelHeight,
                    mXAxis.labelRotationAngle)


            mXAxis.mLabelWidth = labelWidth.roundToInt()
            mXAxis.mLabelHeight = labelHeight.roundToInt()
            mXAxis.mLabelRotatedWidth = labelRotatedSize.width.roundToInt()
            mXAxis.mLabelRotatedHeight = labelRotatedSize.height.roundToInt()

            FSize.recycleInstance(labelRotatedSize)
            FSize.recycleInstance(labelSize)
        } else {
            super.computeSize()
        }
    }
}