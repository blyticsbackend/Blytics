package com.nbt.blytics.activity.qrcode

import com.google.gson.annotations.SerializedName

/**
 * Created bynbton 19-11-2021
 */
data class QrResponse(
    @SerializedName("data")
    val `data`: Data,
    @SerializedName("status")
    val status: String
) {
    data class Data(
        @SerializedName("qrcode")
        val qrcode: String
    )
}

data class QrRequest(
    val user_id:String,
    val user_token:String
)