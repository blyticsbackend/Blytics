package com.nbt.blytics.modules.home
import com.google.gson.annotations.SerializedName


/**
 * Created bynbton 08-11-2021
 */

data class WalletAccountModel(
    val `data`: MutableList<WalletAccountData>,
    val status: String
) {
    data class WalletAccountData(
        val acc_no: String,
        val acc_uuid: String,
        val active: Boolean
    )
}

data class SpentRequest(

    @SerializedName("user_id")
    val userId: String,
    @SerializedName("user_token")
    val userToken: String,
    @SerializedName("uuid")
    val accUuid: String,
)


data class SpentResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("data")
    val `data`: Data,
    val message:String =""
) {
    data class Data(
        @SerializedName("amount_display")
        val amountDisplay: AmountDisplay,

        @SerializedName("amount_actual")
        val amountActual: AmountActual,

        val month:String
    ){
        data class AmountDisplay(
            @SerializedName("balance")
            val balance: String,
            @SerializedName("todays_receive")
            val todaysReceive: String,
            @SerializedName("monthly_received")
            val monthlyReceived: String,
            @SerializedName("todays_spent")
            val todaysSpent: String,
            @SerializedName("monthly_spent")
            val monthlySpent: String
        )
        data class AmountActual(
            val balance: String
        )
    }
}

data class BalanceRequest(
    @SerializedName("acc_uuid")
    val accUuid: String,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("user_token")
    val userToken: String
)

data class BalanceResponse(
    @SerializedName("data")
    val `data`: Data,
    @SerializedName("status")
    val status: String
) {
    data class Data(
        @SerializedName("balance")
        val balance: String
    )
}

data class CircleModel(
    val balance :String,
    val spent :String
)

