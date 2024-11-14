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
import com.nbt.blytics.countrypicker.CountryCodePicker.Companion.countryArray
import com.nbt.blytics.countrypicker.adpters.CountryCodeAdapter
import com.nbt.blytics.countrypicker.models.Country
import com.nbt.blytics.databinding.CountryCodePickerDialogBinding


class CountryCodeDialog(context: Context, val callback: (Country) -> Unit) :
    Dialog(context) {

    private val TAG = CountryCodeDialog::class.java.simpleName
    private lateinit var binding: CountryCodePickerDialogBinding
    private lateinit var adapter: CountryCodeAdapter
    private val filterList: MutableList<Country> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.country_code_picker_dialog,
            null,
            false
        )

        filterList.addAll(countryArray)
        binding.searchEdt.doOnTextChanged { text, start, count, after ->
            Log.d(TAG, text.toString())
            Log.d(TAG, start.toString())
            Log.d(TAG, count.toString())
            Log.d(TAG, after.toString())
            filterList.clear()
            for (i in countryArray.indices) {
                if (countryArray[i].toString().contains(text.toString(), true)) {
                    filterList.add(countryArray[i])
                }
            }
            adapter.notifyDataSetChanged()

        }
        setContentView(binding.root)
        setAdapter()

    }

    private fun setAdapter() {
        adapter = CountryCodeAdapter(context, filterList) { pos ->
            callback(filterList[pos])
        }
        binding.countryDialogRv.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.countryDialogRv.adapter = adapter
    }


}