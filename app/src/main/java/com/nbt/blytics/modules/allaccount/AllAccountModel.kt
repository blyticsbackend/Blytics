package com.nbt.blytics.modules.allaccount
import com.google.gson.annotations.SerializedName


data class AllAccountModel(
    val `data`: MutableList<AllAccountData>,
    val status: String
) {
    data class AllAccountData(
        val acc_no: String ,
        val acc_uuid: String,
        val active: Boolean,
        val deactivate_at: String,
        val default: Boolean,
        val purpose: String,
        val tracker_info: String,
        val withdraw_date: String
    )
}


data class CurrentAccountModel(
    val `data`: MutableList<CurrentAccountData>,
    val status: String
) {
    data class CurrentAccountData(
        val acc_no: String,
        val default: Boolean,
        val acc_uuid: String,
        val active: Boolean
    )
}
data class LinkedAccResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("data")
    val `data`: Data

) {
    data class Data(
        val acc_list :MutableList<AccList>,
        val address:Address
    )
    data class Address(
        val address:String,
        val state: String,
        val country :String,
        val pin_code :String
    )
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
        var expire:Boolean,
        @SerializedName("withdrawl_limit")
        val withdrawlLimit: String,
        @SerializedName("amount")
        val amount:String,
        var isFront:Boolean = true
    )
}




data class SendAccountData(var user_id: String, var user_token: String, var acc_type: String)

data class UpdateDefaultAcc (
    var user_id: String,
    var user_token: String,
    var acc_uuid: String,
    var is_default: Boolean=true)

data class GetUpdateDefaultAcc(var status:String,var message:String,var data:UpdateResponse)



data class UpdateResponse (var acc_num:String,var uuid:String)



