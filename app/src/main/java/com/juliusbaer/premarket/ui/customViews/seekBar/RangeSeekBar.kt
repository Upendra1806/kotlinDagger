package com.juliusbaer.premarket.ui.customViews.seekBar

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.ui.fragments.extentions.format
import timber.log.Timber
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

class RangeSeekBar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private var markTextMargin: Float = 0f

    var showHintLabels: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                requestLayout()
            }
        }
    var showMarkLabels: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                requestLayout()
            }
        }

    //default seekbar's padding left and right
    private var DEFAULT_PADDING_LEFT_AND_RIGHT: Int = 0

    private val defaultPaddingTop: Int
    // The background of the progress
    private var mProgressHintBGId: Int = R.drawable.bg_baloon_white_round_3dp

    //    The background of the Drag button
    private var mThumbResId: Int = 0

    @IdRes
    private var markResId: Int = 0


    //Scale mode:
    // number according to the actual proportion of the number of arranged;
    // other equally arranged
    private var mCellMode: Int = 0


    //single is Seekbar mode, range is angeSeekbar
    //single = 1; range = 2
    private var mSeekBarMode: Int = 0


    //The default is 1, and when it is greater than 1,
    // it will automatically switch back to the scale mode
    private var cellsCount = 1


    //The spacing between the scale and the progress bar
    private val textPadding: Int


    //The progress indicates the distance between the background and the button
    private val mHintBGPadding: Int

    private val mSeekBarHeight: Int
    private var mThumbSize: Int = 0


    //The minimum distance between two buttons
    private var reserveCount: Int = 0

    private var mCursorTextHeight: Int = 0
    private var mPartLength: Int = 0
    private var heightNeeded: Int = 0
    private val lineCorners: Int
    private var lineWidth: Int = 0


    // the color of the selected progress bar
    private var colorLineSelected: Int = 0


    // the color of the unselected progress bar
    private var colorLineEdge: Int = 0

    //The foreground color of progress bar and thumb button.
    private var colorPrimary: Int = 0

    //The background color of progress bar and thumb button.
    private var colorSecondary: Int = 0


    //Scale text and prompt text size
    private val mTextSize: Int

    private val lineTop: Int
    private val lineBottom: Int
    private var lineLeft: Int = 0
    private var lineRight: Int = 0


    //Progress prompted the background height, width,
    // if it is 0, then adaptively adjust
    private var mHintBGHeight: Float = 0.toFloat()
    private val mHintBGWith: Float

    private var offsetValue: Float = 0.toFloat()
    private var cellsPercent: Float = 0.toFloat()
    private var reserveValue: Float = 0.toFloat()
    private var reservePercent: Float = 0.toFloat()
    var maxValue: Float = 0.toFloat()
    var minValue: Float = 0.toFloat()


    //True maximum and minimum values
    var min: Float = 0.toFloat()

    var max: Float = 0.toFloat()


    private var isEnable = true

    private var mProgressHintMode: Int = 0

    //The texts displayed on the scale
    private val mTextArray: Array<CharSequence>?
    var markList = listOf<Pair<Float, String?>>()
        set(value) {
            if (field != value) {
                field = value

                invalidate()
            }
        }

    private var markDrawable: Drawable? = null
    private var markClickListener: ((progress: Float, label: String?) -> Unit)? = null

    private var mProgressHintBG: Drawable? = null
    private val mMainPaint = Paint()
    private val mCursorPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private var mProgressPaint: Paint? = null
    private val line = RectF()
    var leftSB: SeekBar? = null
    var rightSB: SeekBar? = null
    private var currTouch: SeekBar? = null

    private var callback: OnRangeChangedListener? = null
    private val marksPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    val currentRange: FloatArray
        get() {
            val range = maxValue - minValue
            return if (mSeekBarMode == 2) {
                floatArrayOf(-offsetValue + minValue + range * leftSB!!.currPercent, -offsetValue + minValue + range * rightSB!!.currPercent)
            } else {
                floatArrayOf(-offsetValue + minValue + range * leftSB!!.currPercent, -offsetValue + minValue + range * 1.0f)
            }
        }

    private val minTapSize = dp2px(context, 16f)

    init {
        val t = context.obtainStyledAttributes(attrs, R.styleable.CustomSeekBar)
        cellsCount = t.getInt(R.styleable.CustomSeekBar_cells, 1)
        reserveValue = t.getFloat(R.styleable.CustomSeekBar_reserve, 0f)
        min = t.getFloat(R.styleable.CustomSeekBar_min, 0f)
        max = t.getFloat(R.styleable.CustomSeekBar_max, 100f)
        mThumbResId = t.getResourceId(R.styleable.CustomSeekBar_thumbResId, 0)
        val markResId = t.getResourceId(R.styleable.CustomSeekBar_markResId, 0)
        if (markResId > 0) {
            setMarkResId(markResId)
        }
        mProgressHintBGId = t.getResourceId(R.styleable.CustomSeekBar_progressHintResId, mProgressHintBGId)
        colorLineSelected = t.getColor(R.styleable.CustomSeekBar_lineColorSelected, -0xb4269e)
        colorLineEdge = t.getColor(R.styleable.CustomSeekBar_lineColorEdge, -0x282829)
        colorPrimary = t.getColor(R.styleable.CustomSeekBar_thumbPrimaryColor, 0)
        colorSecondary = t.getColor(R.styleable.CustomSeekBar_thumbSecondaryColor, 0)
        mTextArray = t.getTextArray(R.styleable.CustomSeekBar_markTextArray)
        mProgressHintMode = t.getInt(R.styleable.CustomSeekBar_progressHintMode, HINT_MODE_DEFAULT)
        textPadding = t.getDimension(R.styleable.CustomSeekBar_textPadding, dp2px(context, 7f).toFloat()).toInt()
        mTextSize = t.getDimension(R.styleable.CustomSeekBar_textSize, dp2px(context, 12f).toFloat()).toInt()
        mHintBGHeight = t.getDimension(R.styleable.CustomSeekBar_hintBGHeight, 0f)
        mHintBGWith = t.getDimension(R.styleable.CustomSeekBar_hintBGWith, 0f)
        mSeekBarHeight = t.getDimension(R.styleable.CustomSeekBar_seekBarHeight, dp2px(context, 2f).toFloat()).toInt()
        mHintBGPadding = t.getDimension(R.styleable.CustomSeekBar_hintBGPadding, 0f).toInt()
        mThumbSize = t.getDimension(R.styleable.CustomSeekBar_thumbSize, dp2px(context, 26f).toFloat()).toInt()
        mCellMode = t.getInt(R.styleable.CustomSeekBar_cellMode, 0)
        mSeekBarMode = t.getInt(R.styleable.CustomSeekBar_seekBarMode, 2)

        showMarkLabels = t.getBoolean(R.styleable.CustomSeekBar_showMarkLabels, showMarkLabels)
        showHintLabels = t.getBoolean(R.styleable.CustomSeekBar_showHintLabels, showHintLabels)
        markTextMargin = t.getDimension(R.styleable.CustomSeekBar_markTextMargin, dp2px(context, 4f).toFloat())

        val markTextColor = t.getColor(R.styleable.CustomSeekBar_markTextColor, 0)
        setMarkTextColor(markTextColor)

        val markTextSize = t.getDimension(R.styleable.CustomSeekBar_markTextSize, dp2px(context, 12f).toFloat())
        setMarkTextSize(markTextSize)

        val fontId = t.getResourceId(R.styleable.CustomSeekBar_markFontFamily, -1)
        if (fontId > 0) {
            marksPaint.typeface = ResourcesCompat.getFont(context, fontId)
        }
        if (mSeekBarMode == 2) {
            leftSB = SeekBar(-1)
            rightSB = SeekBar(1)
        } else {
            leftSB = SeekBar(-1)
        }

        // if you don't set the mHintBGWith or the mHintBGWith < default value, if will use default value
        DEFAULT_PADDING_LEFT_AND_RIGHT = if (mHintBGWith == 0f) {
            dp2px(context, 15f)
        } else {
            max((mHintBGWith / 2 + dp2px(context, 5f)).toInt(), dp2px(context, 15f))
        }

        setRules(min, max, reserveValue, cellsCount)
        initPaint()
        initBitmap()
        t.recycle()

        defaultPaddingTop = 0//mSeekBarHeight / 2

        mHintBGHeight = /*if (mProgressHintMode == HINT_MODE_ALWAYS_HIDE && mTextArray == null) {
            mCursorPaint.measureText("国")
        } else {*/
                if (mHintBGHeight == 0f) mCursorPaint.measureText("国") * 2.2f else mHintBGHeight
        //}

        lineTop = mHintBGHeight.toInt() + mThumbSize / 2 - mSeekBarHeight / 2
        lineBottom = lineTop + mSeekBarHeight
        lineCorners = ((lineBottom - lineTop) * 0.45f).toInt()

    }

    fun setMarkClickListener(markClickListener: ((progress: Float, label: String?) -> Unit)?) {
        this.markClickListener = markClickListener
    }

    private fun setMarkTextSize(markTextSize: Float) {
        if (marksPaint.textSize != markTextSize) {
            marksPaint.textSize = markTextSize
            if (showMarkLabels || showHintLabels) invalidate()
        }
    }

    private fun setMarkTextColor(markTextColor: Int) {
        if (marksPaint.color != markTextColor) {
            marksPaint.color = markTextColor
            if (showMarkLabels || showHintLabels) invalidate()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        heightNeeded = lineTop + mSeekBarHeight + mThumbSize / 2
        if (showMarkLabels || showHintLabels) {
            val fm = marksPaint.fontMetrics
            val markTextHeight = fm.bottom - fm.top + fm.leading
            heightNeeded += (markTextMargin + markTextHeight).toInt()
        }

        /**
         * onMeasure widthMeasureSpec heightMeasureSpec
         * MeasureSpec.EXACTLY
         * MeasureSpec.AT_MOST
         * MeasureSpec.UNSPECIFIED
         */
        heightSize = when (heightMode) {
            MeasureSpec.EXACTLY -> MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY)
            MeasureSpec.AT_MOST -> MeasureSpec.makeMeasureSpec(heightNeeded, MeasureSpec.AT_MOST)
            else -> MeasureSpec.makeMeasureSpec(
                    heightNeeded, MeasureSpec.EXACTLY)
        }
        super.onMeasure(widthMeasureSpec, heightSize)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // Calculates the position of the progress bar and initializes the positions of
        // the two buttons based on it
        lineLeft = DEFAULT_PADDING_LEFT_AND_RIGHT + paddingLeft
        lineRight = w - lineLeft - paddingRight
        lineWidth = lineRight - lineLeft
        line.set(lineLeft.toFloat(), lineTop.toFloat(), lineRight.toFloat(), lineBottom.toFloat())

        leftSB!!.onSizeChanged(lineLeft, lineBottom, mThumbSize, lineWidth, cellsCount > 1, mThumbResId, context)
        if (mSeekBarMode == 2) {
            rightSB!!.onSizeChanged(lineLeft, lineBottom, mThumbSize, lineWidth, cellsCount > 1, mThumbResId, context)
        }
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Draw the scales, and according to the current position is set within
        // the scale range of different color display

        if (mTextArray != null) {
            mPartLength = lineWidth / (mTextArray.size - 1)
            for (i in mTextArray.indices) {
                val text2Draw = mTextArray[i].toString()
                val x: Float

                if (mCellMode == 1) {
                    mCursorPaint.color = colorLineEdge
                    x = lineLeft + i * mPartLength - mCursorPaint.measureText(text2Draw) / 2
                } else {
                    val num = java.lang.Float.parseFloat(text2Draw)
                    val result = currentRange
                    if (compareFloat(num, result[0]) != -1 && compareFloat(num, result[1]) != 1 && mSeekBarMode == 2) {
                        mCursorPaint.color = ContextCompat.getColor(context, R.color.colorAccent)
                    } else {
                        mCursorPaint.color = colorLineEdge
                    }

                    x = lineLeft + lineWidth * (num - min) / (max - min) - mCursorPaint.measureText(text2Draw) / 2
                }
                val y = (lineTop - textPadding).toFloat()
                canvas.drawText(text2Draw, x, y, mCursorPaint)
            }
        }

        // draw the progress bar
        mMainPaint.color = colorLineEdge
        canvas.drawRoundRect(line, lineCorners.toFloat(), lineCorners.toFloat(), mMainPaint)
        mMainPaint.color = colorLineSelected
        if (mSeekBarMode == 2) {
            canvas.drawRoundRect(leftSB!!.left.toFloat() + (leftSB!!.widthSize / 2).toFloat() + leftSB!!.lineWidth * leftSB!!.currPercent, lineTop.toFloat(),
                    rightSB!!.left.toFloat() + (rightSB!!.widthSize / 2).toFloat() + rightSB!!.lineWidth * rightSB!!.currPercent, lineBottom.toFloat(),
                    lineCorners.toFloat(), lineCorners.toFloat(), mMainPaint)
        } else {
            canvas.drawRoundRect((leftSB!!.left + leftSB!!.widthSize / 2).toFloat(), lineTop.toFloat(),
                    leftSB!!.left.toFloat() + (leftSB!!.widthSize / 2).toFloat() + leftSB!!.lineWidth * leftSB!!.currPercent, lineBottom.toFloat(),
                    lineCorners.toFloat(), lineCorners.toFloat(), mMainPaint)
        }
        val marksLabelsY = if (showMarkLabels) {
            val fm = marksPaint.fontMetrics
            val markTextHeight = abs(fm.top) + fm.leading
            lineBottom + markTextHeight + markTextMargin + mThumbSize / 2
        } else {
            0f
        }
        for ((markProgress, markLabel) in markList) {
            val markX = lineLeft + lineWidth * (markProgress - min) / (max - min)
            markDrawable?.let {
                val x = when (markProgress) {
                    min -> markX
                    max -> markX - it.intrinsicWidth
                    else -> markX - it.intrinsicWidth / 2
                }
                val y = lineTop.toFloat() - it.intrinsicHeight / 2 + mSeekBarHeight / 2
                it.setBounds(x.toInt(), y.toInt(), x.toInt() + it.intrinsicWidth, y.toInt() + it.intrinsicHeight)
                it.draw(canvas)
            }
            if (showMarkLabels && !markLabel.isNullOrEmpty()) {
                val x = markX - marksPaint.measureText(markLabel) / 2
                canvas.drawText(markLabel, x, marksLabelsY, marksPaint)
            }
        }
        leftSB!!.draw(canvas)
        if (mSeekBarMode == 2) {
            rightSB!!.draw(canvas)
        }
    }

    /**
     * init the paints
     */
    private fun initPaint() {
        mMainPaint.style = Paint.Style.FILL
        mMainPaint.color = colorLineEdge

        mCursorPaint.style = Paint.Style.FILL
        mCursorPaint.color = colorLineEdge
        if (!isInEditMode) {
            mCursorPaint.typeface = ResourcesCompat.getFont(context, R.font.verlag_book)
        }
        mCursorPaint.textSize = mTextSize.toFloat()

        mProgressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mProgressPaint!!.typeface = Typeface.DEFAULT


        //Calculate the height of the text
        val fm = mCursorPaint.fontMetrics
        mCursorTextHeight = (ceil((fm.descent - fm.ascent).toDouble()) + 2).toInt()
    }


    private fun initBitmap() {
        mProgressHintBG = ContextCompat.getDrawable(context, mProgressHintBGId)
    }

    //*********************************** SeekBar ***********************************//

    inner class SeekBar(position: Int) {
        var lineWidth: Int = 0
        var widthSize: Int = 0
        private var heightSize: Int = 0
        var left: Int = 0
        private var right: Int = 0
        private var top: Int = 0
        private var bottom: Int = 0
        var currPercent: Float = 0.toFloat()
        var material = 0f
        var isShowingHint: Boolean = false
        private var isLeft: Boolean = false
        private var bmp: Bitmap? = null
        private var anim: ValueAnimator? = null
        private var shadowGradient: RadialGradient? = null
        private var defaultPaint: Paint? = null
        var mHintText2Draw: String? = null
        var labelText2Draw: String? = null
        private val progressLabelPaint = Paint().apply {
            style = Paint.Style.FILL
            color = ContextCompat.getColor(context, R.color.background)
        }

        private var isPrimary: Boolean? = true

        internal val te: TypeEvaluator<Int> = TypeEvaluator { fraction, startValue, endValue ->
            val alpha = (Color.alpha(startValue!!) + fraction * (Color.alpha(endValue!!) - Color.alpha(startValue))).toInt()
            val red = (Color.red(startValue) + fraction * (Color.red(endValue) - Color.red(startValue))).toInt()
            val green = (Color.green(startValue) + fraction * (Color.green(endValue) - Color.green(startValue))).toInt()
            val blue = (Color.blue(startValue) + fraction * (Color.blue(endValue) - Color.blue(startValue))).toInt()
            Color.argb(alpha, red, green, blue)
        }


        init {
            isLeft = position < 0
        }

        /**
         * Calculates the position and size of each button
         *
         * @param x
         * @param y
         * @param hSize
         * @param parentLineWidth
         * @param cellsMode
         * @param bmpResId
         * @param context
         */
        fun onSizeChanged(x: Int, y: Int, hSize: Int, parentLineWidth: Int, cellsMode: Boolean, bmpResId: Int, context: Context) {
            heightSize = hSize
            widthSize = heightSize
            left = x - widthSize / 2
            right = x + widthSize / 2
            top = y - heightSize / 2
            bottom = y + heightSize / 2

            lineWidth = if (cellsMode) {
                parentLineWidth
            } else {
                parentLineWidth
            }

            if (bmpResId > 0) {
                val original = BitmapFactory.decodeResource(context.resources, bmpResId)

                if (original != null) {
                    val matrix = Matrix()
                    val scaleHeight = mThumbSize * 1.0f / original.height
                    matrix.postScale(scaleHeight, scaleHeight)
                    bmp = Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true)
                }

            } else {
                defaultPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                val radius = (widthSize * DEFAULT_RADIUS).toInt()
                val barShadowRadius = (radius * 0.95f).toInt()
                val mShadowCenterX = widthSize / 2
                val mShadowCenterY = heightSize / 2
                shadowGradient = RadialGradient(mShadowCenterX.toFloat(), mShadowCenterY.toFloat(), barShadowRadius.toFloat(), Color.BLACK, Color.TRANSPARENT, Shader.TileMode.CLAMP)
            }
        }


        /**
         * Draw buttons and tips for background and text
         * @param canvas
         */
        fun draw(canvas: Canvas) {
            val offset = (lineWidth * currPercent).toInt()
            canvas.save()
            canvas.translate(offset.toFloat(), 0f)
            var text2Draw = ""
            var hintW: Int
            val hintH: Int = mHintBGHeight.toInt()
            val result = currentRange

            if (isLeft) {

                text2Draw = if (mHintText2Draw == null) {
                    when (typeBage) {
                        FLOAT_TYPE -> result[0].toDouble().format(2) + ""
                        INT_TYPE -> result[0].toInt().toString() + ""
                        else -> result[0].toInt().toString() + ""
                    }
                } else {
                    mHintText2Draw as String
                }

                // if is the start，change the thumb color
                isPrimary = compareFloat(result[0], min) == 0

            } else {
                if (mHintText2Draw == null) {
                    when (typeBage) {
                        FLOAT_TYPE -> result[1].toDouble().format(2) + ""
                        INT_TYPE -> result[1].toInt().toString() + ""
                        else -> result[1].toInt().toString() + ""
                    }
                } else {
                    text2Draw = mHintText2Draw as String
                }

                isPrimary = compareFloat(result[1], max) == 0
            }

            hintW = (if (mHintBGWith == 0f)
                mCursorPaint.measureText(text2Draw) + DEFAULT_PADDING_LEFT_AND_RIGHT
            else
                mHintBGWith).toInt()

            if (hintW < 1.5f * hintH) hintW = (1.5f * hintH).toInt()

            Timber.d("text: $text2Draw, mHintText2Draw $mHintText2Draw")
            if (bmp != null) {
                canvas.drawBitmap(bmp!!, left.toFloat(), (lineTop - bmp!!.height / 2).toFloat(), null)
                if (isShowingHint) {

                    val rect = Rect()
                    rect.left = left - (hintW / 2 - bmp!!.width / 2)
                    rect.top = bottom - hintH - bmp!!.height
                    rect.right = rect.left + hintW
                    rect.bottom = rect.top + hintH
                    mProgressHintBG?.bounds = rect
                    mProgressHintBG?.draw(canvas)
//                    mCursorPaint.color = Color.WHITE
                    mCursorPaint.color = ContextCompat.getColor(context, R.color.deepBlue)

                    val x = (left + bmp!!.width / 2 - mCursorPaint.measureText(text2Draw) / 2).toInt()
                    val y = bottom - hintH - bmp!!.height + hintH / 2
                    canvas.drawText(text2Draw, x.toFloat(), y.toFloat(), mCursorPaint)
                }
            } else {
                canvas.translate(left.toFloat(), 0f)
                if (isShowingHint) {
                    val rect = Rect()
                    rect.left = widthSize / 2 - hintW / 2
                    rect.top = defaultPaddingTop
                    rect.right = rect.left + hintW
                    rect.bottom = rect.top + hintH
                    mProgressHintBG?.bounds = rect
                    mProgressHintBG?.draw(canvas)

//                    mCursorPaint.color = Color.WHITE
                    mCursorPaint.color = ContextCompat.getColor(context, R.color.deepBlue)

                    val x = (widthSize / 2 - mCursorPaint.measureText(text2Draw) / 2).toInt()

                    //Here and the background shape, temporarily based on the shape of this figure ratio calculation
                    val y = hintH / 3 + defaultPaddingTop + mCursorTextHeight / 2

                    canvas.drawText(text2Draw, x.toFloat(), y.toFloat(), mCursorPaint)
                }
                if (!labelText2Draw.isNullOrEmpty()) {
                    val textWidth = marksPaint.measureText(labelText2Draw)
                    val x = (widthSize / 2 - textWidth / 2).toInt()
                    val fm = marksPaint.fontMetrics
                    val markTextHeight = abs(fm.top) + fm.leading
                    val y = lineBottom  + markTextMargin + mThumbSize / 2
                    val space = dp2px(context, 2f)
                    canvas.drawRect(x.toFloat() - space, y, x + textWidth + space * 2, y + markTextHeight, progressLabelPaint)
                    canvas.drawText(labelText2Draw, x.toFloat(), y + markTextHeight, marksPaint)
                }
                drawDefault(canvas)
            }

            canvas.restore()
        }

        /**
         *
         * If there is no image resource, draw the default button
         * @param canvas
         */
        private fun drawDefault(canvas: Canvas) {

            val centerX = widthSize / 2
            val centerY = lineBottom - mSeekBarHeight / 2
            val radius = (widthSize * DEFAULT_RADIUS).toInt()
            // draw shadow
            defaultPaint!!.style = Paint.Style.FILL
            canvas.save()
            canvas.translate(0f, radius * 0.25f)
            canvas.scale(1 + 0.1f * material, 1 + 0.1f * material, centerX.toFloat(), centerY.toFloat())
            defaultPaint!!.shader = shadowGradient
            canvas.drawCircle(centerX.toFloat(), centerY.toFloat(), radius.toFloat(), defaultPaint!!)
            defaultPaint!!.shader = null
            canvas.restore()
            // draw body
            defaultPaint!!.style = Paint.Style.FILL
            if (isPrimary!!) {
                //if not set the color，it will use default color
                if (colorPrimary == 0) {
                    defaultPaint!!.color = te.evaluate(material, -0x1, -0x181819)
                } else {
                    defaultPaint!!.color = colorPrimary
                }
            } else {
                if (colorSecondary == 0) {
                    defaultPaint!!.color = te.evaluate(material, -0x1, -0x181819)
                } else {
                    defaultPaint!!.color = colorSecondary
                }
            }
            canvas.drawCircle(centerX.toFloat(), centerY.toFloat(), radius.toFloat(), defaultPaint!!)
            // draw border
            defaultPaint!!.style = Paint.Style.STROKE
            defaultPaint!!.color = -0x282829
            canvas.drawCircle(centerX.toFloat(), centerY.toFloat(), radius.toFloat(), defaultPaint!!)

        }

        /**
         * @param event
         * @return
         */
        fun collide(event: MotionEvent): Boolean {

            val x = event.x
            val y = event.y
            val offset = (lineWidth * currPercent).toInt()
            return x > left + offset && x < right + offset && y > top && y < bottom
        }

        fun slide(percentInput: Float) {
            var percent = percentInput
            if (percent < 0)
                percent = 0f
            else if (percent > 1) percent = 1f
            currPercent = percent
        }


        fun materialRestore() {
            if (anim != null) anim!!.cancel()
            anim = ValueAnimator.ofFloat(material, 0f)
            anim!!.addUpdateListener { animation ->
                material = animation.animatedValue as Float
                invalidate()
            }
            anim!!.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    material = 0f
                    invalidate()
                }
            })
            anim!!.start()
        }

        fun setProgressHint(hint: String) {
            mHintText2Draw = hint
        }
    }

    //*********************************** SeekBar ***********************************//

    interface OnRangeChangedListener {
        fun onRangeChanged(view: RangeSeekBar, min: Float, max: Float, isFromUser: Boolean)
    }


    fun setOnRangeChangedListener(listener: OnRangeChangedListener) {
        callback = listener
    }

    fun setValue(minInput: Float, maxInput: Float) {
        var min = minInput
        var max = maxInput
        min += offsetValue
        max += offsetValue

        if (min < minValue) {
            throw IllegalArgumentException("setValue() min < (preset min - offsetValue) . #min:$min #preset min:$minValue #offsetValue:$offsetValue")
        }
        if (max > maxValue) {
            throw IllegalArgumentException("setValue() max > (preset max - offsetValue) . #max:$max #preset max:$maxValue #offsetValue:$offsetValue")
        }

        if (reserveCount > 1) {
            if ((min - minValue) % reserveCount != 0f) {
                throw IllegalArgumentException("setValue() (min - preset min) % reserveCount != 0 . #min:$min #preset min:$minValue#reserveCount:$reserveCount#reserve:$reserveValue")
            }
            if ((max - minValue) % reserveCount != 0f) {
                throw IllegalArgumentException("setValue() (max - preset min) % reserveCount != 0 . #max:$max #preset min:$minValue#reserveCount:$reserveCount#reserve:$reserveValue")
            }
            leftSB!!.currPercent = (min - minValue) / reserveCount * cellsPercent
            if (mSeekBarMode == 2) {
                rightSB!!.currPercent = (max - minValue) / reserveCount * cellsPercent
            }
        } else {
            leftSB!!.currPercent = if (maxValue > minValue) (min - minValue) / (maxValue - minValue) else 0f
            if (mSeekBarMode == 2) {
                rightSB!!.currPercent = if (maxValue > minValue) (max - minValue) / (maxValue - minValue) else 1f
            }
        }
        if (callback != null) {
            if (mSeekBarMode == 2) {
                callback!!.onRangeChanged(this, leftSB!!.currPercent, rightSB!!.currPercent, false)
            } else {
                callback!!.onRangeChanged(this, leftSB!!.currPercent, leftSB!!.currPercent, false)
            }
        }
        invalidate()
    }

    fun setValue(value: Float) {
        setValue(value, max)
    }

    fun setRange(min: Float, max: Float) {
        setRules(min, max, reserveCount.toFloat(), cellsCount)
    }

    fun setLineColor(colorLineEdge: Int, colorLineSelected: Int) {
        this.colorLineEdge = colorLineEdge
        this.colorLineSelected = colorLineSelected
    }

    fun setThumbPrimaryColor(thumbPrimaryColor: Int) {
        this.colorPrimary = thumbPrimaryColor
    }

    fun setThumbSecondaryColor(thumbSecondaryColor: Int) {
        this.colorSecondary = thumbSecondaryColor
    }

    fun setCellsCount(cellsCount: Int) {
        this.cellsCount = cellsCount
    }

    fun setThumbSize(mThumbSize: Int) {
        this.mThumbSize = mThumbSize
    }

    fun setLineWidth(lineWidth: Int) {
        this.lineWidth = lineWidth
    }

    fun setProgressHintMode(mProgressHintMode: Int) {
        this.mProgressHintMode = mProgressHintMode
    }

    fun setProgressHintBGId(mProgressHintBGId: Int) {
        this.mProgressHintBGId = mProgressHintBGId
    }

    fun setThumbResId(mThumbResId: Int) {
        this.mThumbResId = mThumbResId
    }

    fun setMarkResId(@DrawableRes markResId: Int) {
        if (this.markResId != markResId) {
            this.markResId = markResId
            markDrawable = ContextCompat.getDrawable(context, markResId)
        }
    }

    fun setCellMode(mCellMode: Int) {
        this.mCellMode = mCellMode
    }

    fun setSeekBarMode(mSeekBarMode: Int) {
        this.mSeekBarMode = mSeekBarMode
    }

    fun setRules(min: Float, max: Float, reserve: Float, cells: Int) {
        var min = min
        var max = max
        if (max <= min) {
            throw IllegalArgumentException("setRules() max must be greater than min ! #max:$max #min:$min")
        }
        this.max = max
        this.min = min
        if (min < 0) {
            offsetValue = 0 - min
            min += offsetValue
            max += offsetValue
        }
        minValue = min
        maxValue = max

        if (reserve < 0) {
            throw IllegalArgumentException("setRules() reserve must be greater than zero ! #reserve:$reserve")
        }
        if (reserve >= max - min) {
            throw IllegalArgumentException("setRules() reserve must be less than (max - min) ! #reserve:" + reserve + " #max - min:" + (max - min))
        }
        if (cells < 1) {
            throw IllegalArgumentException("setRules() cells must be greater than 1 ! #cells:$cells")
        }
        cellsCount = cells
        cellsPercent = 1f / cellsCount
        reserveValue = reserve
        reservePercent = reserve / (max - min)
        reserveCount = (reservePercent / cellsPercent + if (reservePercent % cellsPercent != 0f) 1 else 0).toInt()
        if (cellsCount > 1) {
            if (mSeekBarMode == 2) {
                if (leftSB!!.currPercent + cellsPercent * reserveCount <= 1 && leftSB!!.currPercent + cellsPercent * reserveCount > rightSB!!.currPercent) {
                    rightSB!!.currPercent = leftSB!!.currPercent + cellsPercent * reserveCount
                } else if (rightSB!!.currPercent - cellsPercent * reserveCount >= 0 && rightSB!!.currPercent - cellsPercent * reserveCount < leftSB!!.currPercent) {
                    leftSB!!.currPercent = rightSB!!.currPercent - cellsPercent * reserveCount
                }
            } else {
                if (1 - cellsPercent * reserveCount >= 0 && 1 - cellsPercent * reserveCount < leftSB!!.currPercent) {
                    leftSB!!.currPercent = 1 - cellsPercent * reserveCount
                }
            }
        } else {
            if (mSeekBarMode == 2) {
                if (leftSB!!.currPercent + reservePercent <= 1 && leftSB!!.currPercent + reservePercent > rightSB!!.currPercent) {
                    rightSB!!.currPercent = leftSB!!.currPercent + reservePercent
                } else if (rightSB!!.currPercent - reservePercent >= 0 && rightSB!!.currPercent - reservePercent < leftSB!!.currPercent) {
                    leftSB!!.currPercent = rightSB!!.currPercent - reservePercent
                }
            } else {
                if (1 - reservePercent >= 0 && 1 - reservePercent < leftSB!!.currPercent) {
                    leftSB!!.currPercent = 1 - reservePercent
                }
            }
        }
        invalidate()
    }


    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        this.isEnable = enabled
    }

    fun setProgressDescription(progress: String) {
        if (leftSB != null) {
            leftSB!!.setProgressHint(progress)
        }
        if (rightSB != null) {
            rightSB!!.setProgressHint(progress)
        }
    }

    fun setLeftProgressDescription(progress: String) {
        if (leftSB != null) {
            leftSB!!.setProgressHint(progress)
            invalidate()
        }
    }

    fun setRightProgressDescription(progress: String) {
        if (rightSB != null) {
            rightSB!!.setProgressHint(progress)
            invalidate()
        }
    }

    private fun isShowProgressHint(seekBar: SeekBar?, isEnable: Boolean) {

        when (mProgressHintMode) {
            HINT_MODE_DEFAULT -> seekBar!!.isShowingHint = isEnable
            HINT_MODE_ALWAYS_SHOW -> seekBar!!.isShowingHint = true
            HINT_MODE_ALWAYS_HIDE -> seekBar!!.isShowingHint = false
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnable) return true
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                var touchResult = false
                if (rightSB != null && rightSB!!.currPercent >= 1 && leftSB!!.collide(event)) {
                    currTouch = leftSB
                    touchResult = true

                } else if (rightSB != null && rightSB!!.collide(event)) {
                    currTouch = rightSB
                    touchResult = true

                } else if (leftSB!!.collide(event)) {
                    currTouch = leftSB
                    touchResult = true

                }

                //Intercept parent TouchEvent
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true)
                }
                if (!touchResult && markClickListener != null && markDrawable != null) {
                    val markThumb = markDrawable!!
                    val thumbWidth = max(minTapSize, markThumb.intrinsicWidth)
                    val thumbHeight = max(minTapSize, markThumb.intrinsicHeight)

                    for ((markProgress, markLabel) in markList) {
                        val markX = lineLeft + lineWidth * (markProgress - min) / (max - min)

                        val x = when (markProgress) {
                            min -> markX
                            max -> markX - thumbWidth
                            else -> markX - thumbWidth / 2
                        }
                        val y = lineTop.toFloat() - thumbHeight / 2 + mSeekBarHeight / 2
                        if (event.x >= x && event.x <= x + thumbWidth
                                && event.y >= y && event.y <= y + thumbHeight) {

                            markClickListener?.invoke(markProgress, markLabel)
                            return true
                        }
                    }
                }
                return touchResult
            }
            MotionEvent.ACTION_MOVE -> if (currTouch != null) {
                var percent: Float
                val x = event.x

                currTouch!!.material = if (currTouch!!.material >= 1) 1f else currTouch!!.material + 0.1f

                if (currTouch === leftSB) {
                    if (cellsCount > 1) {
                        percent = if (x < lineLeft) {
                            0f
                        } else {
                            (x - lineLeft) * 1f / lineWidth
                        }
                        var touchLeftCellsValue = (percent / cellsPercent).roundToInt()
                        val currRightCellsValue: Int = if (mSeekBarMode == 2) {
                            (rightSB!!.currPercent / cellsPercent).roundToInt()
                        } else {
                            (1.0f / cellsPercent).roundToInt()
                        }
                        percent = touchLeftCellsValue * cellsPercent

                        while (touchLeftCellsValue > currRightCellsValue - reserveCount) {
                            touchLeftCellsValue--
                            if (touchLeftCellsValue < 0) break
                            percent = touchLeftCellsValue * cellsPercent
                        }
                    } else {
                        percent = if (x < lineLeft) {
                            0f
                        } else {
                            (x - lineLeft) * 1f / lineWidth
                        }
                        if (mSeekBarMode == 2) {
                            if (percent > rightSB!!.currPercent - reservePercent) {
                                percent = rightSB!!.currPercent - reservePercent
                            }
                        } else {
                            if (percent > 1.0f - reservePercent) {
                                percent = 1.0f - reservePercent
                            }
                        }
                    }
                    leftSB!!.slide(percent)
                    isShowProgressHint(leftSB, true)

                    //Intercept parent TouchEvent
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true)
                    }
                } else if (currTouch === rightSB) {
                    if (cellsCount > 1) {
                        percent = if (x > lineRight) {
                            1f
                        } else {
                            (x - lineLeft) * 1f / lineWidth
                        }
                        var touchRightCellsValue = (percent / cellsPercent).roundToInt()
                        val currLeftCellsValue = (leftSB!!.currPercent / cellsPercent).roundToInt()
                        percent = touchRightCellsValue * cellsPercent

                        while (touchRightCellsValue < currLeftCellsValue + reserveCount) {
                            touchRightCellsValue++
                            if (touchRightCellsValue > maxValue - minValue) break
                            percent = touchRightCellsValue * cellsPercent
                        }
                    } else {
                        percent = if (x > lineRight) {
                            1f
                        } else {
                            (x - lineLeft) * 1f / lineWidth
                        }
                        if (percent < leftSB!!.currPercent + reservePercent) {
                            percent = leftSB!!.currPercent + reservePercent
                        }
                    }
                    rightSB!!.slide(percent)
                    isShowProgressHint(rightSB, true)
                }

                if (callback != null) {
                    val result = currentRange
                    callback!!.onRangeChanged(this, result[0], result[1], true)
                }
                invalidate()

                //Intercept parent TouchEvent
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true)
                }
            }
            MotionEvent.ACTION_CANCEL -> if (currTouch != null) {
                if (mSeekBarMode == 2) {
                    isShowProgressHint(rightSB, false)
                }
                isShowProgressHint(leftSB, false)
                if (callback != null) {
                    val result = currentRange
                    callback!!.onRangeChanged(this, result[0], result[1], false)
                }

                //Intercept parent TouchEvent
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true)
                }
            }
            MotionEvent.ACTION_UP -> if (currTouch != null) {
                if (mSeekBarMode == 2) {
                    isShowProgressHint(rightSB, false)
                }
                isShowProgressHint(leftSB, false)
                currTouch!!.materialRestore()

                if (callback != null) {
                    val result = currentRange
                    callback!!.onRangeChanged(this, result[0], result[1], false)
                }

                //Intercept parent TouchEvent
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true)
                }
                currTouch = null
            }
        }
        return super.onTouchEvent(event)
    }

    public override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val ss = SavedState(superState)
        ss.minValue = minValue - offsetValue
        ss.maxValue = maxValue - offsetValue
        ss.reserveValue = reserveValue
        ss.cellsCount = cellsCount
        val results = currentRange
        ss.currSelectedMin = results[0]
        ss.currSelectedMax = results[1]
        return ss
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        val ss = state as SavedState
        super.onRestoreInstanceState(ss.superState)
        val min = ss.minValue
        val max = ss.maxValue
        val reserve = ss.reserveValue
        val cells = ss.cellsCount
        setRules(min, max, reserve, cells)
        val currSelectedMin = ss.currSelectedMin
        val currSelectedMax = ss.currSelectedMax
        setValue(currSelectedMin, currSelectedMax)
    }

    private inner class SavedState : BaseSavedState {
        var minValue: Float = 0.0f
        var maxValue: Float = 0.0f
        var reserveValue: Float = 0.0f
        var cellsCount: Int = 0
        var currSelectedMin: Float = 0.0f
        var currSelectedMax: Float = 0.0f

        internal constructor(superState: Parcelable) : super(superState)

        private constructor(`in`: Parcel) : super(`in`) {
            minValue = `in`.readFloat()
            maxValue = `in`.readFloat()
            reserveValue = `in`.readFloat()
            cellsCount = `in`.readInt()
            currSelectedMin = `in`.readFloat()
            currSelectedMax = `in`.readFloat()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeFloat(minValue)
            out.writeFloat(maxValue)
            out.writeFloat(reserveValue)
            out.writeInt(cellsCount)
            out.writeFloat(currSelectedMin)
            out.writeFloat(currSelectedMax)
        }
    }

    private fun dp2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    private fun compareFloat(a: Float, b: Float): Int {

        val ta = (a * 1000).roundToInt()
        val tb = (b * 1000).roundToInt()
        return when {
            ta > tb -> 1
            ta < tb -> -1
            else -> 0
        }
    }

    private var typeBage: String = ""

    fun setTypeBage(type: String) {
        typeBage = type
    }

    companion object {
        //        private var typeBage = ""
        const val DATE_TYPE: String = "DATE_TYPE"
        const val FLOAT_TYPE: String = "FLOAT_TYPE"
        const val INT_TYPE: String = "INT_TYPE"
        //the progress hint mode
        //defaultMode: show hint when you move the thumb
        //alwaysHide: hide progress hint all the time
        //alwaysShow: show progress hint all the time
        const val HINT_MODE_DEFAULT = 0
        const val HINT_MODE_ALWAYS_HIDE = 1
        const val HINT_MODE_ALWAYS_SHOW = 2

        private const val DEFAULT_RADIUS = 0.5f
    }

}