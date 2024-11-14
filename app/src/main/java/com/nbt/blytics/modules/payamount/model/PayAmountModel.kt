package com.nbt.blytics.modules.payamount.model

import com.google.gson.annotations.SerializedName


/**
 * Created bynbton 29-10-2021
 */
data class SendMoneyRequest(
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("Buuid_send")
    val buuidSend: String,
    @SerializedName("tpin_verified")
    val tpinVerified: Boolean,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("user_receive")
    val userReceive: List<UserReceive>,
    @SerializedName("user_token")
    val userToken: String,
    val chargeable: String = "",
val reference:String =""
) {
    data class UserReceive(
        @SerializedName("receive_phone_no")
        val receivePhoneNo: String
    )
}

data class SendMoneyResponse(

    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message: String = "",
    @SerializedName("error_code")
    val errorCode: String = "",
    val data: SendMoneyResponse.Data?
) {
    data class Data(
        val chargeable: Boolean
    )
}

data class CheckValidTnxTpinRequest(
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("user_token")
    val userToken: String,
    @SerializedName("uuid")
    val uuid: String,
    val tpin: String
)

data class RequestMoney(
    @SerializedName("requested_amount")
    val requestedAmount: Double,
    @SerializedName("requested_status")
    val requestedStatus: String,
    @SerializedName("requested_to_user")
    val requestedToUser: List<RequestedToUser>,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("user_token")
    val userToken: String
) {
    data class RequestedToUser(
        @SerializedName("requested_to")
        val requestedTo: String
    )
}

data class RequestMoneyResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: String
)