package com.nbt.blytics.countrypicker

import com.nbt.blytics.countrypicker.models.Country
import com.nbt.blytics.countrypicker.models.CountriesStates

/**
 * Created bynbton 19-07-2021
 */
interface CallbackSelectCountry {
    fun selectedCountry(country: Country)
}
interface CallbackCountrySelected{
    fun selectedCountry(country: CountriesStates.Country)
}

interface CallbackSelectState {
    fun selectedState(state: CountriesStates.Country.State)
}