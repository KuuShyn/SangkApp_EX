package com.thesis.sangkapp_ex

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent

class CustomImageView(context: Context, attrs: AttributeSet) : androidx.appcompat.widget.AppCompatImageView(context, attrs) {
    private var mListener: ((MotionEvent) -> Boolean)? = null

    fun setOnCustomTouchListener(listener: (MotionEvent) -> Boolean) {
        mListener = listener
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Optionally handle the ACTION_DOWN event if needed
                return true
            }
            MotionEvent.ACTION_UP -> {
                // Call performClick when ACTION_UP is detected
                performClick()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        // Call the super method to ensure accessibility services can trigger the click
        super.performClick()

        // Call the listener to handle any custom touch logic (if any)
        mListener?.let { listener ->
            listener(MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, 0f, 0f, 0))
        }

        return true
    }
}
