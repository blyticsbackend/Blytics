package com.nbt.blytics.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.os.Handler
import android.os.Message
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import com.nbt.blytics.R


/**
 * Created bynbton 23-07-2021
 */
class TooltipWindow(val ctx :Context) {
    private val MSG_DISMISS_TOOLTIP = 100
    private var tipWindow: PopupWindow = PopupWindow(ctx)
    private var contentView: View
    private var inflater: LayoutInflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    init  {
        contentView = inflater.inflate(R.layout.tooltip_layout, null)
    }

    fun showToolTip(anchor: View) {
        tipWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
        tipWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT)
        tipWindow.setOutsideTouchable(true)
        tipWindow.isTouchable = true
        tipWindow.isFocusable = true
        tipWindow.setBackgroundDrawable(BitmapDrawable())
        tipWindow.setContentView(contentView)
        val screen_pos = IntArray(2)
        // Get location of anchor view on screen
        anchor.getLocationOnScreen(screen_pos)

        // Get rect for anchor view
        val anchor_rect = Rect(
            screen_pos[0], screen_pos[1], screen_pos[0]
                    + anchor.getWidth(), screen_pos[1] + anchor.getHeight()
        )

        // Call view measure to calculate how big your view should be.
        contentView.measure(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val contentViewHeight: Int = contentView.getMeasuredHeight()
        val contentViewWidth: Int = contentView.getMeasuredWidth()
        // In this case , i dont need much calculation for x and y position of
        // tooltip
        // For cases if anchor is near screen border, you need to take care of
        // direction as well
        // to show left, right, above or below of anchor view
        val position_x: Int = anchor_rect.centerX() - contentViewWidth / 2
        val position_y: Int = anchor_rect.bottom - anchor_rect.height() / 2
        tipWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, position_x, position_y)

        // send message to handler to dismiss tipWindow after X milliseconds
        handler.sendEmptyMessageDelayed(MSG_DISMISS_TOOLTIP, 4000)
    }

    fun isTooltipShown(): Boolean {
        return tipWindow.isShowing()
    }

    fun dismissTooltip() {
        if (tipWindow.isShowing) tipWindow.dismiss()
    }

    var handler: Handler =
    object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_DISMISS_TOOLTIP -> if (tipWindow.isShowing()) tipWindow.dismiss()
            }
        }
    }

}