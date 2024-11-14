package com.nbt.blytics.modules.selftransfer
import com.google.gson.annotations.SerializedName
import java.util.stream.Stream


/**
 * Created by Nbt on 30-11-2021
 */
data class SelfTxnRequest(
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("user_token")
    val userToken: String,
    @SerializedName("uuid_receive")
    val uuidReceive: String,
    @SerializedName("uuid_send")
    val uuidSend: String,
    @SerializedName("chargeable")
    val chargeable: Boolean,
    @SerializedName("amount")
    val amount: String,
    val tpin_verified:Boolean = true,
    val reference:String =""
)

data class SelfTxnResponse(
    val status :String,
    val message :String,
    val error_code:String,
    val data:Data?
){
    data class Data(
        val chargeable:Boolean
    )
}

data class WalletResponse(
    val acc_no:String,
    val uuid:String
)