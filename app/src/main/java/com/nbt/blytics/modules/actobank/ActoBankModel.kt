package com.nbt.blytics.modules.actobank
import com.google.gson.annotations.SerializedName


/**
 * Created by Nbt on 19-11-2021
 */
data class SendMoneyBankRequest(
    @SerializedName("amount")
    val amount: String,
    @SerializedName("Euser")
    val euser: Euser,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("user_token")
    val userToken: String,
    @SerializedName("uuid")
    val uuid: String,
    val chargeable:Boolean,
    val tpin_verified:Boolean,
    val reference:String=""
) {
    data class Euser(
        @SerializedName("destbankcode")
        val destbankcode: String,
        @SerializedName("recipientaccount")
        val recipientaccount: String,
        val   user_name: String,
        @SerializedName("destbankname")
        val destbankname: String,


        )
}
data class SendMoneyBankRespone(
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: String,
    val error_code:String,
    val data:Data?

){
    data class Data(
        @SerializedName("chargeable")
        val chargeable: Boolean,
    )
}
data class CheckAcRequest(
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("user_token")
    val userToken: String,
    @SerializedName("accountnumber")
    val accountnumber: String
)
data class CheckAcResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("accountname")
    val accountname: String,
    @SerializedName("accountnumber")
    val accountnumber: String
)
