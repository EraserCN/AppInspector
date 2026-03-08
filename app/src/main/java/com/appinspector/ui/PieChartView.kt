package com.appinspector.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.appinspector.util.MethodColors

class PieChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val rect = RectF()
    private var segments: List<PieSegment> = emptyList()
    private var totalCount = 0

    data class PieSegment(val color: Int, val percentage: Float)

    fun setData(stats: List<MethodStat>) {
        val total = stats.sumOf { it.count }
        if (total == 0) {
            segments = emptyList()
            invalidate()
            return
        }
        totalCount = total

        val result = mutableListOf<PieSegment>()
        var otherPercentage = 0f
        
        stats.sortedByDescending { it.count }.forEach { stat ->
            val p = stat.count.toFloat() / total
            if (p < 0.03f) {
                otherPercentage += p
            } else {
                result.add(PieSegment(MethodColors.chipTextColorFor(stat.method), p))
            }
        }

        if (otherPercentage > 0) {
            result.add(PieSegment(0xFF9E9E9E.toInt(), otherPercentage)) // Gray for "Other"
        }

        segments = result
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (segments.isEmpty()) return

        val size = minOf(width, height).toFloat()
        val margin = 20f
        rect.set(
            (width - size) / 2 + margin,
            (height - size) / 2 + margin,
            (width + size) / 2 - margin,
            (height + size) / 2 - margin
        )

        var startAngle = -90f
        segments.forEach { segment ->
            val sweepAngle = segment.percentage * 360f
            paint.color = segment.color
            canvas.drawArc(rect, startAngle, sweepAngle, true, paint)
            startAngle += sweepAngle
        }

        // Draw center hole
        paint.color = 0x00000000 // We'll clear it or draw background color
        // For dynamic colors, it's better to use a transparent hole or match window background
        // But since we are on a solid surface, let's use a common surface color or just make it a pie.
        // To be safe with Monet, let's just use a slightly smaller circle with a clear color if possible,
        // or just draw a solid pie for simplicity and compatibility.
    }
}
