package com.nbt.blytics.countrypicker

import android.content.Context
import com.google.gson.Gson
import com.nbt.blytics.countrypicker.models.Country
import java.io.IOException
import java.util.*

/**
 * Created bynbton 07-07-2021
 */
class CountryCodePicker(val context: Context) {
    private val TAG = CountryCodePicker::class.java.simpleName

    private lateinit var dialog: CountryCodeDialog
    private var _callbackSelectCountry: CallbackSelectCountry? = null


    fun init() {
        fillData()
        dialog = CountryCodeDialog(context) { country ->
            dialog.dismiss()
            _callbackSelectCountry?.let {
                it.selectedCountry(country)
            }

        }
        dialog.show()

    }


    fun setOnCountryChangeListener(callbackSelectCountry: CallbackSelectCountry) {
        _callbackSelectCountry = callbackSelectCountry
    }


    private fun fillData() {
        val country = getJsonDataFromAsset(context, "countrycodes.json")
        val gson = Gson()
        countryArray =
            gson.fromJson(country, Array<Country>::class.java)

    }


    private fun getJsonDataFromAsset(context: Context, fileName: String): String? {
        val jsonString: String
        try {
            jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
        return jsonString
    }

    companion object {
        lateinit var countryArray: Array<Country>
    }
}

