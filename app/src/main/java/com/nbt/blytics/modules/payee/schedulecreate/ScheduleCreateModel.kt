package com.nbt.blytics.modules.payee.schedulecreate
import com.google.gson.annotations.SerializedName



data class InternalSchedule(
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("user_receive")
    val userReceive: List<UserReceive>,
    @SerializedName("schedule_amount")
    val scheduleAmount: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("reference")
    val reference: String,
    @SerializedName("user_token")
    val userToken: String,
    @SerializedName("frequency")
    val frequency: String,
    @SerializedName("tpin_verified")
    val tpinVerified: Boolean,
    @SerializedName("end_date")
    val endDate: String,
    @SerializedName("start_date")
    val startDate: String,
    @SerializedName("Buuid_send")
    val buuidSend: String,
    val schedule_id:String="",
    var txn_type:String =""
) {
    data class UserReceive(
        @SerializedName("receive_phone_no")
        val receivePhoneNo: String
    )
}


data class SelfSchedule(
    @SerializedName("schedule_amount")
    val scheduleAmount: String,
    @SerializedName("chargeable")
    val chargeable: Boolean =false,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("user_token")
    val userToken: String,
    @SerializedName("uuid_receive")
    val uuidReceive: String,
    @SerializedName("uuid_send")
    val uuidSend: String,
    @SerializedName("tpin_verified")
    val tpinVerified: Boolean=true,
    @SerializedName("reference")
    val reference: String,
    @SerializedName("txn_type")
    val txnType: String,
    @SerializedName("start_date")
    val startDate: String,
    @SerializedName("end_date")
    val endDate: String,
    @SerializedName("frequency")
    val frequency: String,
    @SerializedName("name")
    val name: String
)


data class ExternalSchedule(
    @SerializedName("start_date")
    val startDate: String,
    @SerializedName("end_date")
    val endDate: String,
    @SerializedName("reference")
    val reference: String,
    @SerializedName("Euser")
    val euser: Euser,
    @SerializedName("frequency")
    val frequency: String,
    @SerializedName("uuid")
    val uuid: String,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("tpin_verified")
    val tpinVerified: Boolean,
    @SerializedName("user_token")
    val userToken: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("schedule_amount")
    val scheduleAmount: String,
    val schedule_id:String="",
) {
    data class Euser(
        @SerializedName("destbankcode")
        val destbankcode: String,
        @SerializedName("user_name")
        val accountname: String,
        @SerializedName("recipientaccount")
        val recipientaccount: String
    )
}

data class ScheduleCrateRes(
    val status:String,
    val message:String
)

