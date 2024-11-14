package com.nbt.blytics.modules.singletransaction
import com.google.gson.annotations.SerializedName


/**
 * Created bynbton 19-10-2021
 */
data class TransactionModel(
    val userName: String,
    val date: String,
    val amount: String,
    val status: String
)
