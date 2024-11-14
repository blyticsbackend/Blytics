package com.nbt.blytics.modules.otp.model

import com.google.gson.annotations.SerializedName

/**
 * Created bynbton 23-07-2021
 */

data class CheckValidTpinRequest(
    @SerializedName("user_id")
    val userId:String,
    val tpin:String
)
data class CheckValidTpinResponse(
    val status:String,
    val message:String,
    @SerializedName("error_code")
    val errorCode:String="",
    val data: Data?
){
    data class Data(
        val verified:Boolean
    )
}