package com.nbt.blytics.countrypicker

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.nbt.blytics.countrypicker.models.CountriesStates

/**
 * Created by Nbt on 07-07-2021
 */
class StatePicker(val _context: Context) {
    private val TAG = StatePicker::class.java.simpleName


    private lateinit var dialog: StateDialog
    private var _callbackSelectState: CallbackSelectState? = null
    var selectStateData: CountriesStates.Country.State? = null


     fun init() {
        dialog = StateDialog(_context) { state ->
            dialog.dismiss()
            _callbackSelectState?.let {
                it.selectedState(state)
            }
            selectStateData = state

        }
        dialog.show()


    }

    fun setOnStateChangeListener(callbackSelectState: CallbackSelectState) {
        _callbackSelectState = callbackSelectState
    }


}
