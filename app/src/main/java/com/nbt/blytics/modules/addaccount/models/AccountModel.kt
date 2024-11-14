package com.nbt.blytics.modules.addaccount.models

import com.google.gson.annotations.SerializedName

/**
 * Created bynbton 29-06-2021
 */
data class CreateAcRequest(
    @SerializedName("user_id")
    val userId:String,
    @SerializedName("user_token")
    val userToken:String,
 /*   @SerializedName("acc_purpose")
    val accPurpose:String,*/
    @SerializedName("acc_name")
    val accName:String,
    @SerializedName("is_default")
    val isDefault:Boolean,
  /*  @SerializedName("withdraw_date")
    val withdrawData:String,*/
    val tpin:String
)

data class CreateCurrentAcRequest(
    @SerializedName("user_id")
    val userId:String,
    @SerializedName("user_token")
    val userToken:String,
    @SerializedName("is_default")
    val isDefault:Boolean,
    val tpin:String
)

data class CreateCurrentResponse(
    val status: String,
    val message: String,
    val `data`: Data? = null,
) {
    data class Data(
        val accNo: String,
        val accId: String,
        val uuid: String
    )
}

data class CreateAcResponse(
    val status: String,
    val message: String,
    val `data`: Data? = null,
) {
    data class Data(
        val accNo: String,
        val purpose: String,
        val uuid: String,
        @SerializedName("is_default")
        val isDefault: Boolean
    )
}