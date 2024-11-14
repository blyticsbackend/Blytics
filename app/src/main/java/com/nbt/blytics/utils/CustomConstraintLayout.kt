package com.nbt.blytics.utils

import android.content.Context
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout

/**
 * Created bynbton 24-07-2021
 */
class CustomConstraintLayout(context: Context) : ConstraintLayout(context) {

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return super.onTouchEvent(event)
    }
}