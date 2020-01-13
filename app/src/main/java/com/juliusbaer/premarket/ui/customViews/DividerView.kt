package com.juliusbaer.premarket.ui.customViews

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import com.juliusbaer.premarket.R
import kotlin.math.max

class DividerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    var selectedPos = 50f
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }
    var showSelected = false
    private var dividers: Int = 1
    var dividerWidth = 2f
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        //strokeCap = Paint.Cap.ROUND
    }
    private val paintSelected = Paint().apply {
        style = Paint.Style.FILL
    }
    private var orientation: Int = 0

    @ColorInt
    var color: Int = -0x1000000
        set(value) {
            field = value
            paint.color = color
            paintSelected.color = color
        }

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)

        val dashGap: Int
        val dashLength: Int
        val dashThickness: Int

        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.DividerView, 0, 0)

        try {
            showSelected = a.getBoolean(R.styleable.DividerView_showSelected, showSelected)
            selectedPos = a.getFraction(R.styleable.DividerView_selectedPos, 1, 1, selectedPos)
            dividers = max(2, a.getInt(R.styleable.DividerView_dividers, dividers))
            dividerWidth = a.getDimension(R.styleable.DividerView_dividerWidth, dividerWidth)
            dashGap = a.getDimensionPixelSize(R.styleable.DividerView_dashGap, 5)
            dashLength = a.getDimensionPixelSize(R.styleable.DividerView_dashLength, 5)
            dashThickness = a.getDimensionPixelSize(R.styleable.DividerView_dashThickness, 3)
            color = a.getColor(R.styleable.DividerView_color, color)
            orientation = a.getInt(R.styleable.DividerView_orientation, ORIENTATION_HORIZONTAL)
        } finally {
            a.recycle()
        }
        paint.strokeWidth = dashThickness.toFloat()
        paint.pathEffect = DashPathEffect(floatArrayOf(dashLength.toFloat(), dashGap.toFloat()), 0f)
    }

    override fun onDraw(canvas: Canvas) {
        if (orientation == ORIENTATION_HORIZONTAL) {
            val step = height / (dividers - 1)
            for (y in 0..height step step) {
                canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), paint)
            }
            val center = width / 2f
            canvas.drawLine(center, 0f, center, height.toFloat(), paint)

            if (showSelected) {
                val half = dividerWidth / 2f
                val y = selectedPos * height / 100f - half
                canvas.drawRect(0f, y - half, width.toFloat(), y + half, paintSelected)
            }
        } else {
            val step = width / (dividers - 1)
            for (x in 0..width step step) {
                canvas.drawLine(x.toFloat(), 0f, x.toFloat(), height.toFloat(), paint)
            }
            val center = height / 2f
            canvas.drawLine(0f, center, width.toFloat(), center, paint)

            if (showSelected) {
                val half = dividerWidth / 2f
                val x = selectedPos * width / 100f - half
                canvas.drawRect(x - half, 0f, x + half, height.toFloat(), paintSelected)
            }
        }
    }

    companion object {
        var ORIENTATION_HORIZONTAL = 0
        var ORIENTATION_VERTICAL = 1
    }
}