package com.mtuci.vkclock

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.util.AttributeSet
import android.widget.FrameLayout
import java.util.Calendar
import java.util.Timer
import java.util.TimerTask
import kotlin.math.cos
import kotlin.math.sin

class ClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private fun Float.toPx(): Float = (this * context.resources.displayMetrics.density)

    var onTimeChange: (() -> Unit)? = null

    val numbers = listOf("XII", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI")

    var centerPointColor = 0xFFE30611.toInt()
        set(value) {
            field = value
            centerPaint.color = value
        }

    var hourHandPointColor = 0xFF000000.toInt()
        set(value) {
            field = value
            hourPaint.color = value
        }

    var minuteHandPointColor = 0xFF000000.toInt()
        set(value) {
            field = value
            minutePaint.color = value
        }

    var secondHandPointColor = 0xFFE30611.toInt()
        set(value) {
            field = value
            secondPaint.color = value
        }

    var circlePointColor = 0xFF000000.toInt()
        set(value) {
            field = value
            circlePaint.color = value
        }

    var linePointColor = 0xFF000000.toInt()
        set(value) {
            field = value
            linePaint.color = value
        }

    var numbersColor = 0xFF000000.toInt()
        set(value) {
            field = value
            numbersPaint.color = value
        }

    private var centerPointRadius = 2F.toPx()
        set(value) {
            field = value
            centerPaint.strokeWidth = value
        }

    private var hourHandWidth = 2F.toPx()
        set(value) {
            field = value
            hourPaint.strokeWidth = value
        }

    private var minuteHandWidth = 2F.toPx()
        set(value) {
            field = value
            minutePaint.strokeWidth = value
        }
    private var secondHandWidth = 2F.toPx()
        set(value) {
            field = value
            secondPaint.strokeWidth = value
        }

    private var circleWidth = 2F.toPx()
        set(value) {
            field = value
            circlePaint.strokeWidth = value
        }

    private var lineWidth = 2F.toPx()
        set(value) {
            field = value
            linePaint.strokeWidth = value
        }

    private var numbersSize = 20F.toPx()
        set(value) {
            field = value
            numbersPaint.textSize = value
        }

    private var hourHandHeight = 20F.toPx()

    private var minuteHandHeight = 30F.toPx()

    private var secondHandHeight = 40F.toPx()

    private var circleRadius = minuteHandHeight + 20F.toPx()

    private var lineHeight = 10F.toPx()

    private val centerPaint = Paint().apply {
        color = centerPointColor
        strokeWidth = centerPointRadius * 2
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val hourPaint = Paint().apply {
        color = hourHandPointColor
        strokeWidth = hourHandWidth
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val minutePaint = Paint().apply {
        color = minuteHandPointColor
        strokeWidth = minuteHandWidth
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val secondPaint = Paint().apply {
        color = secondHandPointColor
        strokeWidth = secondHandWidth
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val circlePaint = Paint().apply {
        color = circlePointColor
        strokeWidth = circleWidth
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val linePaint = Paint().apply {
        color = linePointColor
        strokeWidth = lineWidth
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val numbersPaint = Paint().apply {
        color = numbersColor
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        textSize = numbersSize
        textAlign = Paint.Align.CENTER
    }

    private val centerPoint = PointF()
    private val hourHandPoint = PointF()
    private val minuteHandPoint = PointF()
    private val secondHandPoint = PointF()

    private var registered: Boolean = false
    private val time: Calendar = Calendar.getInstance()

    init {
        processAttributeSet(attrs)
        setWillNotDraw(false)
    }

    private fun processAttributeSet(attrs: AttributeSet?) {
        context
            .theme
            .obtainStyledAttributes(attrs, R.styleable.ClockView, 0, 0)
            .use {
                centerPointColor = it.getColor(R.styleable.ClockView_centerPointColor, centerPointColor)
                hourHandPointColor = it.getColor(R.styleable.ClockView_hourHandPointColor, hourHandPointColor)
                minuteHandPointColor = it.getColor(R.styleable.ClockView_minuteHandPointColor, minuteHandPointColor)
                secondHandPointColor = it.getColor(R.styleable.ClockView_secondHandPointColor, secondHandPointColor)
                circlePointColor = it.getColor(R.styleable.ClockView_circlePointColor, circlePointColor)
                linePointColor = it.getColor(R.styleable.ClockView_linePointColor, linePointColor)
                numbersColor = it.getColor(R.styleable.ClockView_numbersColor, numbersColor)
                centerPointRadius = it.getDimension(R.styleable.ClockView_centerPointRadius, 0f)
                hourHandWidth = it.getDimension(R.styleable.ClockView_hourHandWidth, 0f)
                minuteHandWidth = it.getDimension(R.styleable.ClockView_minuteHandWidth, 0f)
                secondHandWidth = it.getDimension(R.styleable.ClockView_secondHandWidth, 0f)
                circleWidth = it.getDimension(R.styleable.ClockView_circleWidth, 0f)
                lineWidth = it.getDimension(R.styleable.ClockView_lineWidth, 0f)
                numbersSize = it.getDimension(R.styleable.ClockView_numbersSize, 0f)
            }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (!registered) {
            registered = true
            registerReceiver()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (registered) {
            unregisterReceiver()
            registered = false
        }
    }

    private fun registerReceiver() {
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_TIME_TICK)
        filter.addAction(Intent.ACTION_TIME_CHANGED)
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED)

        context.registerReceiver(receiver, filter)

        val timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                onTimeChanged()
            }
        }, 0, 1000)
    }

    private fun unregisterReceiver() {
        context.unregisterReceiver(receiver)
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            onTimeChanged()
        }
    }

    private fun onTimeChanged() {
        time.timeInMillis = System.currentTimeMillis()
        onTimeChange?.invoke()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        hourHandHeight = w / 4F - hourHandWidth
        minuteHandHeight = w / 3F - minuteHandWidth
        secondHandHeight = w / 2.5F - secondHandWidth
        circleRadius = w / 2F - circleWidth
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {

        calculateSizes()

        repeat(12){ i ->
            val pointStart = getPointByAngle(HOUR_ANGLE * i, circleRadius)
            val pointEnd = getPointByAngle(HOUR_ANGLE * i, circleRadius - lineHeight)
            canvas.drawLine(pointStart.x, pointStart.y, pointEnd.x, pointEnd.y, linePaint)
        }
        repeat(60){ i ->
            val pointStart = getPointByAngle(MINUTE_ANGLE * i, circleRadius)
            val pointEnd = getPointByAngle(MINUTE_ANGLE * i, circleRadius - lineHeight / 2)
            canvas.drawLine(pointStart.x, pointStart.y, pointEnd.x, pointEnd.y, linePaint)
        }
        repeat(12){ i ->
            val textBounds = Rect()
            numbersPaint.getTextBounds(numbers[i], 0, numbers[i].length, textBounds)
            val textHeight = textBounds.height()
            val point = getPointByAngle(HOUR_ANGLE * i, (circleRadius - lineHeight * 2.5).toFloat())
            canvas.drawText(numbers[i] ,point.x, point.y + (textHeight / 2f), numbersPaint)
        }
        canvas.drawLine(centerPoint.x, centerPoint.y, hourHandPoint.x, hourHandPoint.y, hourPaint)
        canvas.drawLine(centerPoint.x, centerPoint.y, minuteHandPoint.x, minuteHandPoint.y, minutePaint)
        canvas.drawLine(centerPoint.x, centerPoint.y, secondHandPoint.x, secondHandPoint.y, secondPaint)
        canvas.drawPoint(centerPoint.x, centerPoint.y, centerPaint)
        canvas.drawCircle(centerPoint.x, centerPoint.y, circleRadius, circlePaint)
    }

    private fun calculateSizes() {
        val second = time.get(Calendar.SECOND)
        val minute = time.get(Calendar.MINUTE)
        val hour = time.get(Calendar.HOUR_OF_DAY) + minute * HOUR_COEFFICIENT

        centerPoint.set(width / 2F, height / 2F)
        hourHandPoint.set(getPointByAngle(hour * HOUR_ANGLE, hourHandHeight))
        minuteHandPoint.set(getPointByAngle(minute * MINUTE_ANGLE, minuteHandHeight))
        secondHandPoint.set(getPointByAngle(second * SECOND_ANGLE, secondHandHeight))
    }

    private fun getPointByAngle(angle: Double, radius: Float): PointF {
        val radians = Math.toRadians(-angle + 180).toFloat()
        return PointF(
            centerPoint.x + sin(radians) * radius,
            centerPoint.y + cos(radians) * radius
        )
    }

    private companion object {
        private const val HOUR_COEFFICIENT = 1 / 60F
        private const val HOUR_ANGLE = 360 / 12.0
        private const val MINUTE_ANGLE = 360 / 60.0
        private const val SECOND_ANGLE = 360 / 60.0
    }
}