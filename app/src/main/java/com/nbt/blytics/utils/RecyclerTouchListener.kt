package com.nbt.blytics.utils

import android.content.Context
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.nbt.blytics.modules.payee.payment.PaymentHistoryAdapter

/**
 * Created bynbton 02-11-2021
 */
class RecyclerTouchListener(val context:Context, val recycleView:RecyclerView, val  clicklistener: PaymentHistoryAdapter.ClickListener?) : RecyclerView.OnItemTouchListener{
    private var  gestureDetector: GestureDetector? =null
    init {
        gestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                return true
            }
            override fun onLongPress(e: MotionEvent) {
                val child = recycleView.findChildViewUnder(e.x, e.y)
                if (child != null && clicklistener != null) {
                    clicklistener.onLongClick(child, recycleView.getChildAdapterPosition(child))
                }
            }
        })
    }

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        val child: View? = rv.findChildViewUnder(e.x, e.y)
        if (child != null && clicklistener != null && gestureDetector!!.onTouchEvent(e)) {
            clicklistener.onClick(child, rv.getChildAdapterPosition(child))
        }
        return false
    }
    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
    }
    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
    }
}