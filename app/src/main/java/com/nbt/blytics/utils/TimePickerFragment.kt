package com.nbt.blytics.utils

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import com.nbt.blytics.R
import java.util.Calendar

/**
 * Created bynbton 11-08-2021
 */
class DatePickerFragment(val activity: Activity,val maxDay:Int=0,val maxMonth :Int =0, val maxYear:Int =0, val minDay:Int=0,val minMonth :Int =0, val minYear:Int =0, val callback: (Int, Int, Int) -> Unit) :
    DialogFragment(), DatePickerDialog.OnDateSetListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val dialog = DatePickerDialog(activity, R.style.DatePickerTheme, this, year, month, day)
        if (minYear != 0) {
            c.set(minYear, minMonth-1, minDay)
            dialog.datePicker.minDate = c.timeInMillis
        }
        if (maxYear != 0) {
            c.set(maxYear, maxMonth-1, maxDay)
            dialog.datePicker.maxDate = c.timeInMillis
        }

        dialog.datePicker.spinnersShown = true
        dialog.datePicker.calendarViewShown = false
        return dialog
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        callback(year, month+1, day)
    }
}
