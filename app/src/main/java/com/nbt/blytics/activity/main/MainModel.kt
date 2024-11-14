package com.nbt.blytics.activity.main

import com.google.gson.annotations.SerializedName

data class UserBlockResponse(

    val status: String,
    val message: String,
    val isBlocked: Boolean,
    val isUnAuth: Boolean,
    val data: Data?
) {
    data class Data(
        val user_id: String,
        val user_token: String,
        val wallet_uuid: String,
        val country_code: String,
        val mobile_no: String,
        val is_primary: Boolean,
        val is_local: Boolean,
        val is_block: Boolean,
        val device_changed: Boolean
    )
}


data class UserBlockRequest(
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("user_token")
    val userToken: String
)

data class UnReadNotification(
    val user_id: Int,
    val user_token: String,
)

data class UnReadNotiRespnose(
    val status: String,
    val unread_notifications: Int = -1
)


data class LogoutRequest(
    val user_id: String,
    val user_token: String,
)

data class LogoutResponse(
    val status: String,
    val message: String
)