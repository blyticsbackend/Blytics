package com.nbt.blytics.modules.editaddress.model
import com.google.gson.annotations.SerializedName


/**
 * Created bynbton 19-07-2021
 */
data class CountriesStates(
    @SerializedName("Countries")
    val countries: List<Country>
) {
    data class Country(
        @SerializedName("CountryName")
        val countryName: String,
        @SerializedName("States")
        val states: List<State>
    ) {
        data class State(
            @SerializedName("Cities")
            val cities: List<String>,
            @SerializedName("StateName")
            val stateName: String
        ){
            override fun toString(): String {
                return stateName
            }
        }

        override fun toString(): String {
            return countryName
        }
    }
}

