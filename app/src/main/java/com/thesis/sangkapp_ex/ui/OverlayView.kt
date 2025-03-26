package com.thesis.sangkapp_ex.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.thesis.sangkapp_ex.BoundingBox
import com.thesis.sangkapp_ex.R

class OverlayView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var results = listOf<BoundingBox>()
    private var boxPaint = Paint()
    private var textBackgroundPaint = Paint()
    private var textPaint = Paint()
    private var bounds = Rect()
    var boundingBoxClickListener: BoundingBoxClickListener? = null

    init {
        initPaints()
    }

    fun clear() {
        results = listOf()
        textPaint.reset()
        textBackgroundPaint.reset()
        boxPaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        textBackgroundPaint.color = Color.BLACK
        textBackgroundPaint.style = Paint.Style.FILL
        textBackgroundPaint.textSize = 50f

        textPaint.color = Color.WHITE
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 50f

        boxPaint.color = ContextCompat.getColor(context!!, R.color.bounding_box_color)
        boxPaint.strokeWidth = 8F
        boxPaint.style = Paint.Style.STROKE
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        results.forEach {
            val left = it.x1 * width
            val top = it.y1 * height
            val right = it.x2 * width
            val bottom = it.y2 * height

            // Draw bounding box
            canvas.drawRect(left, top, right, bottom, boxPaint)

            // Draw label text with confidence score
            val drawableText = "${it.clsName} ${(it.cnf * 100).toInt()}%"
            textBackgroundPaint.getTextBounds(drawableText, 0, drawableText.length, bounds)
            val textWidth = bounds.width()
            val textHeight = bounds.height()

            canvas.drawRect(
                left,
                top,
                left + textWidth + BOUNDING_RECT_TEXT_PADDING,
                top + textHeight + BOUNDING_RECT_TEXT_PADDING,
                textBackgroundPaint
            )
            canvas.drawText(drawableText, left, top + bounds.height(), textPaint)
        }
    }

    fun setResults(boundingBoxes: List<BoundingBox>) {
        results = boundingBoxes
        invalidate()
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x
            val y = event.y

            // Check if touch is within any bounding box
            results.forEach { box ->
                val left = box.x1 * width
                val top = box.y1 * height
                val right = box.x2 * width
                val bottom = box.y2 * height

                if (x in left..right && y in top..bottom) {
                    // Bounding box clicked
                    boundingBoxClickListener?.onBoundingBoxClicked(box)
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    interface BoundingBoxClickListener {
        fun onBoundingBoxClicked(boundingBox: BoundingBox)
    }

    companion object {
        private const val BOUNDING_RECT_TEXT_PADDING = 8
    }
}
