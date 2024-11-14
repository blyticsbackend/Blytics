package com.nbt.blytics.modules.payee.schedule
import com.google.gson.annotations.SerializedName



class RecentScheduleRequest(
    val offset:String,
    val frequent_payee:Boolean,
    val page:String,
    val user_id:Int,
    val user_token:String,
    val uuid:String,

)
data class RecentScheduleRes(

    @SerializedName("status")
    val status: String,
    @SerializedName("data")
    val `data`: Data,
) {
    data class Data(

        @SerializedName("schedule_list")
        val scheduleList: List<Schedule> = emptyList(),
        @SerializedName("total_schedule")
        val totalSchedule: Int=-1,
        @SerializedName("data_remaining")
    val dataRemaining: Int=-1,
    ) {
        data class Schedule(
            @SerializedName("amount")
            val amount: String,
            @SerializedName("name")
            val name: String,
            @SerializedName("next_date")
            val nextDate: String,
            @SerializedName("reference")
            val reference: String,
            @SerializedName("user_id")
            val userId: String,
            val accountnumber:String,
            @SerializedName("end_date")
            val endDate: String,
            @SerializedName("schedule_id")
            val scheduleId: String,
            val frequency:String,
            val type:String,
            val mobNo:String

        )
    }
}

data class DeleteScheduleRequest(
    @SerializedName("schedule_id")
    val scheduleId: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("user_token")
    val userToken: String
)





data class DeleteScheduleResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message: String
)

