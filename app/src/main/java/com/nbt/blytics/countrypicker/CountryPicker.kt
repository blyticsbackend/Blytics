package com.nbt.blytics.countrypicker

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.google.gson.Gson
import com.nbt.blytics.countrypicker.models.CountriesStates
import java.io.IOException

/**
 * Created bynbton 07-07-2021
 */
class CountryPicker(val _context: Context) {
    private val TAG = CountryPicker::class.java.simpleName


    private lateinit var dialog: CountryDialog
    private var _callbackSelectCountry: CallbackCountrySelected? = null



     fun init() {

         fillData()
        dialog = CountryDialog(_context) { country ->
            dialog.dismiss()
            _callbackSelectCountry?.let {
                it.selectedCountry(country)

            }
            selectCountryStateData = country
        }
        dialog.show()


    }

    fun setOnCountryChangeListener(callbackCountrySelected: CallbackCountrySelected) {
        _callbackSelectCountry = callbackCountrySelected
    }


    private fun fillData() {
        val gson = Gson()
        val countryStateJson = getJsonDataFromAsset(_context, "countries_states_city.json")
        countryState =
            gson.fromJson(countryStateJson, CountriesStates::class.java)
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

    fun findCountryDataByName(name:String){
        fillData()
        for(i in countryState.countries.indices){
            if(countryState.countries[i].countryName.equals(name, true)){
                selectCountryStateData = countryState.countries[i]
            }
        }

    }


    companion object {
        lateinit var countryState: CountriesStates
        var selectCountryStateData: CountriesStates.Country? = null

    }
}
