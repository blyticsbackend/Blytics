package com.nbt.blytics.modules.phoneregistation.models
import com.google.gson.annotations.SerializedName


/**
 * Created by Nbt on 03-07-2021
 */


data class CheckExistRequest(
    val user: String="",
    val mob: String,
    @SerializedName("check_for")
    val check_for: Int,
)
data class CheckExistRequest2(
    val user: String="",
    @SerializedName("check_for")
    val check_for: Int,
    val mob:String
)
data class CheckExistEmailModel(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("error_code")
    val errorCode: Int,
    @SerializedName("status")
    val status: String,
    val errorMessage:String=""
) {
    data class Data(
        @SerializedName("email")
        val email: String,
        @SerializedName("mob_no")
        val mobNo: String,
        @SerializedName("user_id")
        val userId: Int
    )
}


data class CheckExistPhoneResponse(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("error_code")
    val errorCode: Int,
    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message:String="",
    val errorMessage:String="",
    val forPreview:Boolean=false
) {
    data class Data(
        @SerializedName("email")
        val email: String,
        @SerializedName("mob_no")
        val mobNo: String,
        @SerializedName("user_id")
        val userId: String,
        @SerializedName("wallet_uuid")
        val walletUUID: String,
        @SerializedName("tpin")
        val tpin: Int,
        @SerializedName("security_question")
        val securityQuestion: Int,
        @SerializedName("first_name")
        val userName: String,
        @SerializedName("avatar_url")
        val avatarUrl: String=""

    )
}

data class CheckUserFullResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("error_code")
    val errorCode: Int,
    @SerializedName("message")
    val message:String="",
    @SerializedName("data")
    val `data`: Data?
) {
    data class Data(
        @SerializedName("user_id")
        val userId: String,
        @SerializedName("first_name")
        val firstName: String,
        @SerializedName("last_name")
        val lastName: String,
        @SerializedName("email")
        val email: String,
        @SerializedName("address")
        val address: String,
        @SerializedName("state")
        val state: String,
        @SerializedName("country")
        val country: String,
        @SerializedName("pin_code")
        val pinCode: String,
        @SerializedName("country_code")
        val countryCode: String,
        @SerializedName("mob_no")
        val mobNo: String,
        @SerializedName("avatar_url")
        val avatarUrl: String
    )
}