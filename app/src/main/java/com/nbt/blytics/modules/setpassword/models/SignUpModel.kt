package com.nbt.blytics.modules.setpassword.models

import com.google.gson.annotations.SerializedName


data class SignUpResponse(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: String
) {
    data class Data(
        @SerializedName("user_id")
        val userId: String,
        @SerializedName("user_token")
        val userToken: String,
        @SerializedName("wallet_uuid")
        val walletUuid: String

    )
}

data class SignUpRequest(
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    val email: String,
    val dob: String,
    val gender: Int,
    @SerializedName("pin_code")
    val pinCode:String,
    val address: String,
    val country: String,
    val state: String,
    val password: String,
    @SerializedName("user_phone")
    val userPhone: UserPhone,
) {
    data class UserPhone(
        @SerializedName("mob_no")
        val mobNo: String,
        @SerializedName("mob_verified")
        val mobVerified: Boolean,
        @SerializedName("device_token")
        val deviceToken: String,
        @SerializedName("device_data")
        val deviceData : DeviceData,
        @SerializedName("country_code")
        val countryCode : String
    )
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
data class SignUpWithEmailRequest(
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,

    val dob: String,
    val gender: Int,
    @SerializedName("pin_code")
    val pinCode:String,
    val address: String,
    val country: String,
    val state: String,
    val password: String,
    @SerializedName("user_phone")
    val userPhone: UserPhone,
) {
    data class UserPhone(
        @SerializedName("mob_no")
        val mobNo: String,
        @SerializedName("mob_verified")
        val mobVerified: Boolean,
        @SerializedName("device_token")
        val deviceToken: String,
        @SerializedName("device_data")
        val deviceData : DeviceData,
        @SerializedName("country_code")
        val countryCode : String
    )
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