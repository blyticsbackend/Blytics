package com.nbt.blytics.modules.securityQes

import android.app.Application
import com.google.gson.annotations.SerializedName
import com.nbt.blytics.base.BaseViewModel



/**
 * Created by Nbt on 06-10-2021
 */


data class PasswordUpdateChangeRequest(
    @SerializedName("user_id")
   val userId:String,
    @SerializedName("user_token")
    val userToken:String,
    @SerializedName("old_password")
    val  oldPassword:String,
    @SerializedName("new_password")
    val  newPassword:String
)

data class PasswordUpdateChangeResponse(
    val status:String,
    val message:String,
    @SerializedName("error_code")
    val errorCode:String=""
)


data class UserSQResponse(
    val data: Data,
    val status: String
) {
    data class Data(
        val quesAns: List<QuesAn>
    ) {
        data class QuesAn(
            val ans: String,
            val hint: String,
            val ques: String
        )
    }
}

data class ChangeTpiRequest(
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("user_token")
    val userToken: String,
    @SerializedName("uuid")
    val accUuid: String,
    @SerializedName("tpin")
    val tpin: String
)

data class ChangeTpiResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message: String
)