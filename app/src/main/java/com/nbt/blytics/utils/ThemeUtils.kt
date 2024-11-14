package com.nbt.blytics.utils

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nbt.blytics.R
import com.nbt.blytics.base.BaseActivity

/**
 * Created bynbton 02-07-2021
 */


enum class THEME(val i: Int) {
    MODE_NIGHT(0),
    MODE_DEFAULT(1),
    MODE_CUSTOM(2)
}

 fun AppCompatActivity.switchToThemeMode(theme: THEME) {
    when (theme.name) {
        THEME.MODE_DEFAULT.name -> {
          //  delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            BaseActivity.isCustomMode = false
            //recreate()
        }
        THEME.MODE_NIGHT.name -> {
            BaseActivity.isCustomMode = false
              /*  delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_YES*/
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        THEME.MODE_CUSTOM.name -> {
            /*delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_NO*/
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            setTheme(R.style.Theme_Blytics_Happy)
            BaseActivity.isCustomMode = true
           // recreate()
        }
    }
}


fun Context.setTextSpanColor(str: String, start: Int, end: Int,color:Int, callback:() ->Unit): SpannableString {
    val clickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View) {
            callback()
        }

    }

    val spannable = SpannableString(str)
    val boldSpan: StyleSpan = StyleSpan(Typeface.BOLD)
    spannable.setSpan(
        clickableSpan,
        start, // start
        end, // end
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    spannable.setSpan(
        boldSpan,
        start, // start
        end, // end
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )

 /*   if(BaseActivity.isCustomMode) {
        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this, color)),
            start, // start
            end, // end
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }else{*/
        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this, color)),
            start, // start
            end, // end
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
   // }
    return spannable

}
/*

fun showThemeDialog() {
    val items = arrayOf("Default", "Night", "Custom")

    MaterialAlertDialogBuilder(this)
        .setTitle("Choose Theme")
        .setItems(items) { dialog, which ->
            when (which) {
                0 -> {
                    switchToThemeMode(THEME.MODE_DEFAULT)
                }
                1 -> {
                    switchToThemeMode(THEME.MODE_NIGHT)
                }
                2 -> {
                    switchToThemeMode(THEME.MODE_CUSTOM)
                }
            }
        }
        .show()
}*/
