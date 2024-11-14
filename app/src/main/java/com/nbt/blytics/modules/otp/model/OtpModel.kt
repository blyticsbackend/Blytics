package com.nbt.blytics.modules.otp.model

import com.google.gson.annotations.SerializedName

data class EmailOtpResponse(
    val status :String,
    val message :String,
    val preview:Boolean=false
)


data class EmailOtpRequest(
    val email:String
)

data class LoginWithMobileRequest(
    @SerializedName("mob_no")
    val mobileNumber:String
)

data class LoginWithMobileResponse(
    @SerializedName("status")
    val status:String,
    @SerializedName("data")
    val data:Data?,
    @SerializedName("error_code")
    val errorCode:String="",
    val message:String=""


){
    data class Data(
        @SerializedName("user_id")
        val userId:String,
        @SerializedName("user_token")
        val userToken:String,
        @SerializedName("uuid")
        val uuid:String
    )
}