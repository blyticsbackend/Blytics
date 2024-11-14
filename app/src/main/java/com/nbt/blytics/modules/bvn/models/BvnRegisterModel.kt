package com.nbt.blytics.modules.bvn.models
import com.google.gson.annotations.SerializedName


/**
 * Created bynbton 21-07-2021
 */
data class BvnRegisterResponse(
    @SerializedName("data")

    val message: String,
    val status: String,
    val `data`: Data?=null,
    val error_code:String =""
) {
    data class Data(
        @SerializedName("data_saved")
        val dataSaved: Boolean,
        @SerializedName("user_permission")
        val userPermission: String
    )
}