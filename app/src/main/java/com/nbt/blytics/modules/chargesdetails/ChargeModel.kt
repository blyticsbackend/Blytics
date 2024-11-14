package com.nbt.blytics.modules.chargesdetails

import com.google.gson.annotations.SerializedName

data class UserChargeRequest(
    val user_id: String,
    val user_token: String,
    val charge_uuid: String,
    val transact_user_id: String,
    val page: String = "",
    val offset: String = ""

)

data class UserChargerResponse(
    @SerializedName("status") val status: String, @SerializedName("data") val `data`: Data
) {
    data class Data(
        @SerializedName("total_data") val totalData: Int,
        @SerializedName("data_remaining") val dataRemaining: Int,
        @SerializedName("final_list") val finalList: List<Final>
    ) {
        data class Final(
            @SerializedName("charged_for") val chargedFor: String,
            @SerializedName("charged_amount") val chargedAmount: String,
            @SerializedName("charged_date") val chargedDate: String
        )
    }
}