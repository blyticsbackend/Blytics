package com.nbt.blytics.modules.payee.payment

import com.google.gson.annotations.SerializedName


/**
 * Created bynbton 14-10-2021
 */

data class PaymentHistory(
    val name: String,
    val date: String,
    val total_amount_sent: String,
    val total_amount_received: String,
    val img: String
)

data class TnxListRequest(
    @SerializedName("frequent_payee") val frequentPayee: String,
    @SerializedName("offset") val offset: String = "",
    @SerializedName("page") val page: Int,
    @SerializedName("user_id") val userId: String,
    @SerializedName("user_token") val userToken: String,
    @SerializedName("uuid") val uuid: String,
    @SerializedName("sort_by") val sortBy: String

)

data class RequestMoneyResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val `data`: List<Data>,
    val message: String = ""
) {
    data class Data(
        @SerializedName("image_url") val imageUrl: String,
        @SerializedName("request_id") val requestId: Int,
        @SerializedName("requested_amount") val requestedAmount: String,
        @SerializedName("requested_by_user_id") val requestedByUserId: Int,
        @SerializedName("request_mob_no") val requestMobNo: String,
        @SerializedName("request_by_user_name") val requestByUserName: String
    )
}

data class RequestMoneyReq(
    @SerializedName("user_id") val userId: Int, @SerializedName("user_token") val userToken: String
)

/**
*status 1 = approve
status 2= cancel
 */
data class ApprovalRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("user_token") val userToken: String,
    val request_id: Int,
    val requested_by_user: Int,
    val requested_status: Int
)

data class ApprovalResponse(
    val status:String
)

