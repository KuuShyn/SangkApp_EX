package com.thesis.sangkapp_ex.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.thesis.sangkapp_ex.R


class ArcProgressBar(context: Context, attributes: AttributeSet) : View(context, attributes) {
    private var progressArc = Paint()
    private var trackArc = Paint()

    private val progressRectf = RectF()

    private var progressWidth = 20f
    private var trackWidth = 20f

    private var progressColorValue = Color.parseColor("#7084ff")
    private var trackColorValue = Color.parseColor("#676767")

    private var progressValue = 50f
    private var progressMax = 100f // Default max progress
    private var progressMin = 0f   // Default min progress
    private var progressValueTmp = 50f

    private var startAngle = 140f
    private var sweepAngle = 260f

    init {
        progressArc.style = Paint.Style.STROKE
        trackArc.style = Paint.Style.STROKE

        val typedArray = context.obtainStyledAttributes(attributes, R.styleable.ArcProgressBar)
        try {
            progressColorValue = typedArray.getColor(R.styleable.ArcProgressBar_progressColor, progressColorValue)
            progressWidth = typedArray.getDimension(R.styleable.ArcProgressBar_progressWidth, progressWidth)

            trackColorValue = typedArray.getColor(R.styleable.ArcProgressBar_trackColor, trackColorValue)
            trackWidth = typedArray.getDimension(R.styleable.ArcProgressBar_trackWidth, trackWidth)

            startAngle = typedArray.getFloat(R.styleable.ArcProgressBar_startAngle, startAngle)
            sweepAngle = typedArray.getFloat(R.styleable.ArcProgressBar_sweepAngle, sweepAngle)

            progressMax = typedArray.getFloat(R.styleable.ArcProgressBar_progressMax, progressMax)
            progressMin = typedArray.getFloat(R.styleable.ArcProgressBar_progressMin, progressMin)
            progressValueTmp = typedArray.getFloat(R.styleable.ArcProgressBar_progress, progressValue)
            progressValue = scale(progressValueTmp, sweepAngle, progressMax, progressMin)
        } finally {
            typedArray.recycle()
        }

        progressArc.strokeWidth = progressWidth
        progressArc.color = progressColorValue

        trackArc.strokeWidth = trackWidth
        trackArc.color = trackColorValue

        progressArc.strokeCap = Paint.Cap.ROUND
        trackArc.strokeCap = Paint.Cap.ROUND
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw the track arc
        canvas.drawArc(progressRectf, startAngle, sweepAngle, false, trackArc)

        // Draw the progress arc
        if (progressValueTmp in progressMin..progressMax) {
            canvas.drawArc(progressRectf, startAngle, progressValue, false, progressArc)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = 200
        val desiredHeight = 200

        val width = resolveSize(desiredWidth, widthMeasureSpec)
        val height = resolveSize(desiredHeight, heightMeasureSpec)

        setMeasuredDimension(width, height)

        val maxStrokeWidth = trackArc.strokeWidth.coerceAtLeast(progressArc.strokeWidth)
        progressRectf.set(
            maxStrokeWidth / 2,
            maxStrokeWidth / 2,
            width.toFloat() - maxStrokeWidth / 2,
            height.toFloat() - maxStrokeWidth / 2
        )
    }

    private fun scale(value: Float, sweepAngle: Float, progressMax: Float, progressMin: Float): Float {
        return ((value - progressMin) * (sweepAngle - 0)) / (progressMax - progressMin)
    }

    // Property to set and get the progress value
    var progress: Float
        get() = progressValue
        set(value) {
            progressValueTmp = value
            progressValue = scale(value, sweepAngle, progressMax, progressMin)
            invalidate()
        }

    // Property to set and get the progress color
    var progressColor: Int
        get() = progressArc.color
        set(value) {
            progressArc.color = value
            invalidate()
        }

    // Property to set and get the track color
    var trackColor: Int
        get() = trackArc.color
        set(value) {
            trackArc.color = value
            invalidate()
        }

    // Property to set and get the maximum progress
    var maxProgress: Float
        get() = progressMax
        set(value) {
            progressMax = value
            progressValue = scale(progressValueTmp, sweepAngle, progressMax, progressMin)
            invalidate()
        }

    // Property to set and get the minimum progress
    var minProgress: Float
        get() = progressMin
        set(value) {
            progressMin = value
            progressValue = scale(progressValueTmp, sweepAngle, progressMax, progressMin)
            invalidate()
        }
}
