package com.nbt.blytics.modules.changepassword.model

import com.google.gson.annotations.SerializedName

/**
 * Created bynbton 08-07-2021
 */

data class ChangePasswordRequest(
    val user:String,
    val password:String
)



data class ChangePasswordResponse(
    val status:String,
    val message:String,
    @SerializedName("error_code")
    val errorCode :String=""
)