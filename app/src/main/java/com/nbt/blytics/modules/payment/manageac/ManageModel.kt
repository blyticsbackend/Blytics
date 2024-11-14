package com.nbt.blytics.modules.payment.manageac
import com.google.gson.annotations.SerializedName


/**
 * Created bynbton 20-12-2021
 */
data class StartTrackerRequest(
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("user_token")
    val userToken: String,
    @SerializedName("start_date")
    val startDate: String,
    @SerializedName("complete_date")
    val completeDate: String,
    @SerializedName("targeted_amount")
    val targetedAmount: String,
    @SerializedName("saving_basis")
    val savingBasis: Int,
    @SerializedName("acc_uuid")
    val accUuid: String
)

data class UpdateManageConfig(
    @SerializedName("acc_uuid")
    val acc_uuid: String,
    @SerializedName("user_id")
    val user_id: Int,
    @SerializedName("user_token")
    val user_token: String,
    @SerializedName("is_active")
    val is_active: Boolean,
    @SerializedName("is_expire")
    val is_expire: Boolean
)

data class StartTrackerResponse(
    val status:String,
    val message:String="",
    val data:Data?,

){
    data class Data(
        val acc_uuid:String,
        val goal_achieve:String
        )
}
data class DefaultAccRequest(
    val user_id: String,
    val user_token:String,
    val uuid: String
)

data class DefaultAccResponse(
    val status:String,
    val message:String
)


data class DefaultWalletRequest(
    val user_id: String,
    val user_token:String,
    val uuid: String
    )

data class DefaultWalletResponse(
    val status:String,
    val message:String
)