package com.nbt.blytics.modules.linkedac.manageac
import com.google.gson.annotations.SerializedName


/**
 * Created bynbton 06-12-2021
 */
data class ManageRequest(
    @SerializedName("acc_uuid")
    val accUuid: String,
    @SerializedName("acc_id")
    val accId: String,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("accessible_days")
    val days: List<Int>,
    @SerializedName("is_active")
    val isActive: Boolean,
    @SerializedName("is_expire")
    val isExpire:Boolean,
    @SerializedName("user_token")
    val userToken: String
)
data class ManageRequestLinked(
    @SerializedName("acc_uuid")
    val accUuid: String,
    @SerializedName("acc_id")
    val accId: String,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("is_active")
    val isActive: Boolean,
    @SerializedName("is_expire")
    val isExpire:Boolean,
    @SerializedName("user_token")
    val userToken: String,
    val deactivate:Boolean
)

data class ManageResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val `data`: Data?
) {
    data class Data(
        @SerializedName("created_by")
        val createdBy: String,
        @SerializedName("created_for")
        val createdFor: String,
        @SerializedName("relation")
        val relation: String,
        @SerializedName("acc_id")
        val accId: String,
        @SerializedName("acc_no")
        val accNo: String,
        @SerializedName("acc_uuid")
        val accUuid: String,
        @SerializedName("active")
        val active: Boolean,
        @SerializedName("expire")
        val expire: Boolean
    )
}

data class GetManageRequest(
    @SerializedName("acc_uuid")
    val accUuid: String,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("user_token")
    val userToken: String,

)

data class GetManageLinkedRequest(
    @SerializedName("acc_uuid")
    val accUuid: String,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("user_token")
    val userToken: String,
    val acc_id:String,
)
data class MangeLinkedResponse(
    val status:String,
    val message:String,
    val data:AccList
) {

    data class AccList(
        @SerializedName("created_for")
        val createdFor: String,
        @SerializedName("relation")
        val relation: String,
        @SerializedName("acc_id")
        val accId: String,
        @SerializedName("acc_no")
        val accNo: String,
        @SerializedName("acc_uuid")
        val accUuid: String,
        @SerializedName("active")
        var active: Boolean,
        @SerializedName("days")
        var days: MutableList<String>,
        @SerializedName("expire")
        var expire: Boolean,
        @SerializedName("withdrawl_limit")
        val withdrawlLimit: String,
        @SerializedName("amount")
        val amount: String,
    )
}



data class ResponseMangeCurrentAc(
    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val `data`: Data?,
    val error_code:String=""
){ data class Data(
    @SerializedName("acc_id")
    val accId: Int,
    @SerializedName("acc_no")
    val accNo: String,
    @SerializedName("acc_uuid")
    val accUuid: String,
    @SerializedName("default")
    val default: Boolean,
    @SerializedName("active")
    val active: Boolean,
    @SerializedName("expire")
    val expire: Boolean,
    @SerializedName("deactivate_at")
    val deactivateAt: String="",
    @SerializedName("withdraw_date")
    val withdrawDate: String="",

)

}
data class ResponseMangeAc(
    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val `data`: Data?,
    val error_code:String=""
) {
    data class Data(
        @SerializedName("acc_id")
        val accId: Int,
        @SerializedName("acc_no")
        val accNo: String,
        @SerializedName("acc_uuid")
        val accUuid: String,
        @SerializedName("default")
        val default: Boolean,
        @SerializedName("withdraw_date")
        val withdrawDate: String,
        @SerializedName("active")
        val active: Boolean,
        @SerializedName("expire")
        val expire: Boolean,
        @SerializedName("deactivate_at")
        val deactivateAt: String,
        @SerializedName("tracker_info")
        val trackerInfo: TrackerInfo
    ) {
        data class TrackerInfo(
            @SerializedName("set")
            val set: Boolean,
            val start_data :String="",
            val complete_data:String="",
            val target_amount:String="",
            val target_achieved:String="",
            val saving_basis:Int=-1,
            val goal_achieved:String=""
        )
    }
}