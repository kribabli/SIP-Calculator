package com.sipcaluculator.View.Widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.sipcaluculator.Model.YearlyBreakdown
import com.sipcaluculator.R
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Lightweight Canvas-based line chart (no external chart library dependency)
 * showing invested amount vs total value growth across the years of an
 * investment. Draws two lines: a flat/step "invested" line and a curved
 * "total value" line, plus year labels along the bottom.
 */
class GrowthChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var investedLineColor: Int = Color.parseColor("#90A4AE")
    private var totalLineColor: Int = Color.parseColor("#1a90ff")

    private var breakdown: List<YearlyBreakdown> = emptyList()
    private var selectedIndex: Int? = null

    /** Called whenever the user taps/drags to a point on the chart, or when new data is set. */
    var onPointSelected: ((YearlyBreakdown) -> Unit)? = null

    private val paddingLeftPx = 12f.dp
    private val paddingRightPx = 12f.dp
    private val paddingTopPx = 16f.dp
    private val paddingBottomPx = 28f.dp

    private val investedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f.dp
        strokeCap = Paint.Cap.ROUND
    }

    private val totalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3.5f.dp
        strokeCap = Paint.Cap.ROUND
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E0E0E0")
        strokeWidth = 1f.dp
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#616161")
        textSize = 10f.dp
        textAlign = Paint.Align.CENTER
    }

    private val highlightLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#9E9E9E")
        style = Paint.Style.STROKE
        strokeWidth = 1.5f.dp
        pathEffect = DashPathEffect(floatArrayOf(6f.dp, 4f.dp), 0f)
    }

    private val highlightDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val highlightDotStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f.dp
        color = Color.WHITE
    }

    init {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.GrowthChartView)
            investedLineColor = typedArray.getColor(
                R.styleable.GrowthChartView_investedLineColor,
                investedLineColor
            )
            totalLineColor = typedArray.getColor(
                R.styleable.GrowthChartView_totalLineColor,
                totalLineColor
            )
            typedArray.recycle()
        }
        investedPaint.color = investedLineColor
        totalPaint.color = totalLineColor

        isClickable = true
        isFocusable = true
    }

    fun setData(data: List<YearlyBreakdown>) {
        breakdown = data
        selectedIndex = if (data.isNotEmpty()) data.lastIndex else null
        invalidate()
        selectedIndex?.let { onPointSelected?.invoke(data[it]) }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (breakdown.isEmpty()) return super.onTouchEvent(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                selectNearestPoint(event.x)
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun selectNearestPoint(touchX: Float) {
        val count = breakdown.size
        if (count == 0) return

        val chartLeft = paddingLeftPx
        val chartRight = width - paddingRightPx
        val stepX = if (count > 1) (chartRight - chartLeft) / (count - 1) else 0f

        val index = if (stepX == 0f) {
            0
        } else {
            ((touchX - chartLeft) / stepX).roundToInt().coerceIn(0, count - 1)
        }

        selectedIndex = index
        invalidate()
        onPointSelected?.invoke(breakdown[index])
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (breakdown.isEmpty()) return

        val chartLeft = paddingLeftPx
        val chartRight = width - paddingRightPx
        val chartTop = paddingTopPx
        val chartBottom = height - paddingBottomPx

        if (chartRight <= chartLeft || chartBottom <= chartTop) return

        val maxValue = max(breakdown.last().totalValue, 1.0)

        // Horizontal grid lines (25/50/75/100%)
        for (fraction in listOf(0.25f, 0.5f, 0.75f, 1f)) {
            val y = chartBottom - (chartBottom - chartTop) * fraction
            canvas.drawLine(chartLeft, y, chartRight, y, gridPaint)
        }

        val investedPath = Path()
        val totalPath = Path()

        val count = breakdown.size
        val stepX = if (count > 1) (chartRight - chartLeft) / (count - 1) else 0f

        val investedPoints = mutableListOf<Pair<Float, Float>>()
        val totalPoints = mutableListOf<Pair<Float, Float>>()

        breakdown.forEachIndexed { index, entry ->
            val x = chartLeft + stepX * index
            val investedY =
                chartBottom - ((entry.investedAmount / maxValue) * (chartBottom - chartTop)).toFloat()
            val totalY =
                chartBottom - ((entry.totalValue / maxValue) * (chartBottom - chartTop)).toFloat()

            investedPoints.add(x to investedY)
            totalPoints.add(x to totalY)

            if (index == 0) {
                investedPath.moveTo(x, investedY)
                totalPath.moveTo(x, totalY)
            } else {
                investedPath.lineTo(x, investedY)
                totalPath.lineTo(x, totalY)
            }
        }

        canvas.drawPath(investedPath, investedPaint)
        canvas.drawPath(totalPath, totalPaint)

        dotPaint.color = totalLineColor
        totalPoints.forEach { (x, y) -> canvas.drawCircle(x, y, 3f.dp, dotPaint) }

        // Highlight the selected (tapped/dragged) year, if any.
        selectedIndex?.let { index ->
            if (index in investedPoints.indices && index in totalPoints.indices) {
                val (selectedX, investedY) = investedPoints[index]
                val (_, totalY) = totalPoints[index]

                canvas.drawLine(selectedX, chartTop, selectedX, chartBottom, highlightLinePaint)

                highlightDotPaint.color = investedLineColor
                canvas.drawCircle(selectedX, investedY, 4.5f.dp, highlightDotPaint)
                canvas.drawCircle(selectedX, investedY, 4.5f.dp, highlightDotStrokePaint)

                highlightDotPaint.color = totalLineColor
                canvas.drawCircle(selectedX, totalY, 5.5f.dp, highlightDotPaint)
                canvas.drawCircle(selectedX, totalY, 5.5f.dp, highlightDotStrokePaint)
            }
        }

        // Year labels along the bottom - first, middle, last to avoid crowding.
        val labelIndices = when {
            count <= 3 -> breakdown.indices.toList()
            else -> listOf(0, count / 2, count - 1)
        }
        labelIndices.forEach { index ->
            val x = chartLeft + stepX * index
            canvas.drawText(
                "Y${breakdown[index].year}",
                x.coerceIn(chartLeft + 10f.dp, chartRight - 10f.dp),
                height.toFloat() - 6f.dp,
                labelPaint
            )
        }
    }

    private val Float.dp: Float
        get() = this * resources.displayMetrics.density
}
