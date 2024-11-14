package com.nbt.blytics.modules.chagnetpin.models

import com.google.gson.annotations.SerializedName


/**
 * Created bynbton 23-07-2021
 */
data class ChangeTipRequest(
    @SerializedName("acc_uuid")
    val accUuid: String,
    val tpin: String,
    @SerializedName("user_id")
    val userId: String
)

data class ChangeTpinResponse(
    val status: String,
    val message: String,
    @SerializedName("error_code")
    val errorCode: String = ""
)

data class CheckTpinResponse(
    val status: String,
    val message: String,
    @SerializedName("error_code")
    val errorCode: String = "",
    val data: Data?=null

) {
    data class Data(var usq:Int,var wtpin:Int)
}

data class CheckTpin(
    @SerializedName("user_id")
    val userId: String,
    val user_token: String,


)
