package com.juliusbaer.premarket.ui.customViews

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatSeekBar
import com.juliusbaer.premarket.R
import kotlin.math.min

class MarkSeekBar @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatSeekBar(context, attrs, defStyleAttr) {
    private var tracking: Boolean = false

    private var markClickListener: (() -> Unit)? = null

    var markThumb: Drawable? = null
        private set

    private var markThumbOffset: Int = 0
    var markProgress: Int = 0
        set(value) {
            field = value
            markThumb?.let {
                setThumbPos(width, it, value / max.toFloat(), Integer.MIN_VALUE)
                invalidate()
            }
        }

    init {
        val t = context.obtainStyledAttributes(attrs, R.styleable.MarkSeekBar)
        markThumb = t.getDrawable(R.styleable.MarkSeekBar_markThumb)
        setMarkThumb(markThumb)

        t.recycle()
    }

    fun setOnMarkClickListener(listener: (() -> Unit)?) {
        markClickListener = listener
    }

    fun setMarkThumb(thumb: Drawable?) {
        val needUpdate: Boolean
        // This way, calling setThumb again with the same bitmap will result in
        // it recalcuating mThumbOffset (if for example it the bounds of the
        // drawable changed)
        if (markThumb != null && thumb !== markThumb) {
            markThumb?.callback = null
            needUpdate = true
        } else {
            needUpdate = false
        }

        if (thumb != null) {
            thumb.callback = this
            markThumbOffset = thumb.intrinsicWidth / 2

            if (needUpdate && (thumb.intrinsicWidth != markThumb!!.intrinsicWidth || thumb.intrinsicHeight != markThumb!!.intrinsicHeight)) {
                requestLayout()
            }
        }

        markThumb = thumb

        invalidate()

        if (needUpdate) {
            updateThumbPos(width, height)
            if (thumb != null && thumb.isStateful) {
                val state = drawableState
                thumb.state = state
            }
        }
    }

    private fun updateThumbPos(w: Int, h: Int) {
        val paddedHeight = h - paddingTop - paddingBottom
        val thumb = markThumb

        // The max height does not incorporate padding, whereas the height
        // parameter does.
        val trackHeight = min(48, paddedHeight)
        val thumbHeight = thumb?.intrinsicHeight ?: 0

        // Apply offset to whichever item is taller.
        val thumbOffset = if (thumbHeight > trackHeight) {
            val offsetHeight = (paddedHeight - thumbHeight) / 2
            offsetHeight
        } else {
            val offsetHeight = (paddedHeight - trackHeight) / 2
            offsetHeight + (trackHeight - thumbHeight) / 2
        }
        if (thumb != null) {
            setThumbPos(w, thumb, getScale(), thumbOffset)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        updateThumbPos(w, h)
    }

    private fun setThumbPos(w: Int, thumb: Drawable, scale: Float, offset: Int) {
        var available = w - paddingStart - paddingEnd
        val thumbWidth = thumb.intrinsicWidth
        val thumbHeight = thumb.intrinsicHeight

        available -= thumbOffset * 2

        val thumbPos = (scale * available + 0.5f).toInt()

        val top: Int
        val bottom: Int
        if (offset == Integer.MIN_VALUE) {
            val oldBounds = thumb.bounds
            top = oldBounds.top
            bottom = oldBounds.bottom
        } else {
            top = offset
            bottom = offset + thumbHeight
        }

        val right = thumbPos + thumbWidth

        // Canvas will be translated, so 0,0 is where we start drawing
        thumb.setBounds(thumbPos, top, right, bottom)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val processed = when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (event.x < thumb.bounds.left
                        || event.x > thumb.bounds.right
                        || event.y < thumb.bounds.top
                        || event.y > thumb.bounds.bottom) {

                    markThumb?.let { markThumb ->
                        if (event.x >= markThumb.bounds.left + paddingStart
                                && event.x <= markThumb.bounds.right + paddingStart
                                && event.y >= markThumb.bounds.top
                                && event.y <= markThumb.bounds.bottom) {

                            markClickListener?.invoke()
                        }
                    }
                    true
                } else {
                    tracking = true
                    false
                }
            }
            MotionEvent.ACTION_UP -> {
                if (tracking) {
                    tracking = false
                    false
                } else {
                    true
                }
            }
            else -> !tracking
        }
        return if (processed) processed else super.onTouchEvent(event)
    }

    @Synchronized
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawMarkThumb(canvas)
    }


    private fun drawMarkThumb(canvas: Canvas) {
        markThumb?.let {
            val saveCount = canvas.save()
            // Translate the padding. For the x, we need to allow the thumb to
            // draw in its extra space
            canvas.translate((paddingStart).toFloat(), paddingTop.toFloat())
            it.draw(canvas)
            canvas.restoreToCount(saveCount)
        }
    }

    private fun getScale(): Float {
        val min = 0
        val max = max
        val range = max - min
        return if (range > 0) (markProgress - min) / range.toFloat() else 0f
    }
}