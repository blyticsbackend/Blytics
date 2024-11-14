package com.nbt.blytics.modules.transactiondetails.models

/**
 * Created bynbton 26-06-2021
 */
data class TranscationDetailsModel(
    val day: String,
    val list: MutableList<Details>

) {
    data class Details(
        val userName: String,
        val date: String,
        val amount: String,
        val status: String
    )
}