package com.nbt.blytics.countrypicker

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Window
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.nbt.blytics.R
import com.nbt.blytics.countrypicker.CountryPicker.Companion.selectCountryStateData
import com.nbt.blytics.countrypicker.adpters.StateAdapter
import com.nbt.blytics.countrypicker.models.CountriesStates
import com.nbt.blytics.databinding.CountryPickerDialogBinding

/**
 * Created bynbton 19-07-2021
 */
class StateDialog (context: Context, val callback: (CountriesStates.Country.State) -> Unit) : Dialog(context) {
    private val TAG = CountryCodeDialog::class.java.simpleName
    private lateinit var binding: CountryPickerDialogBinding
    private lateinit var adapter: StateAdapter
    private val filterList: MutableList<CountriesStates.Country.State> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.country_picker_dialog,
            null,
            false
        )
        binding.titleTv.text ="Select State"
        selectCountryStateData?.apply {
            filterList.addAll(states)
            binding.searchEdt.doOnTextChanged { text, start, count, after ->
                Log.d(TAG, text.toString())
                Log.d(TAG, start.toString())
                Log.d(TAG, count.toString())
                Log.d(TAG, after.toString())
                filterList.clear()
                for (i in states.indices) {
                    if (states[i].toString().contains(text.toString(), true)) {
                        filterList.add(states[i])
                    }
                }
                adapter.notifyDataSetChanged()

            }


        }
        setContentView(binding.root)
        setAdapter()
    }

    private fun setAdapter() {
        adapter = StateAdapter(context, filterList) { pos ->
            callback(filterList[pos])
        }
        binding.countryDialogRv.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.countryDialogRv.adapter = adapter
    }


}