package com.nbt.blytics.modules.signin.model

import com.google.gson.annotations.SerializedName

/**
 * Created bynbton 01-07-2021
 */


data class LoginRequest(
    val user: String,
    val password: String,
    @SerializedName("device_token")
    val deviceToken:String,
    @SerializedName("change_device")
    val changeDevice: Boolean,
    @SerializedName("device_data")
    val deviceData :DeviceData
) {
    data class DeviceData(
        @SerializedName("device_model")
        val deviceModel: String,
        val version: String,
        @SerializedName("api_level")
        val apiLevel: String,
        val fingerprint: String,
        @SerializedName("device_id")
        val deviceId: String

    )
}

data class LoginResponse(
    val status: String,
    val message: String,
    val data: Data?,
    @SerializedName("error_code")
    val errorCode: String = ""
) {
    data class Data(
        @SerializedName("user_id")
        val userId: String,
        @SerializedName("user_token")
        val userToken: String,
        @SerializedName("wallet_uuid")
        val walletUuid: String,
        @SerializedName("mobile_no")
        val mobileNo :String,
        @SerializedName("country_code")
        val countryCode :String,
        @SerializedName("device_changed")
        val deviceChanged :Boolean,
        val isBlock :Boolean


    )
}

data class FailResponse(
    val error: String,
    val message: String
)

