package com.nbt.blytics.modules.notification
import com.google.gson.annotations.SerializedName


/**
 * Created bynbton 15-12-2021
 */
data class NotificationListRequest(
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("user_token")
    val userToken: String,
    @SerializedName("page")
    val page: Int,
    @SerializedName("offset")
    val offset: Int
)



data class AllNotificationResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("data")
    val `data`: Data
) {
    data class Data(
        @SerializedName("notification")
        val notification: Notification
    ) {
        data class Notification(
            @SerializedName("total_data")
            val totalData: Int,
            @SerializedName("data_remaining")
            val dataRemaining: Int,
            @SerializedName("final_list")
            val finalList: List<Final>
        ) {
            data class Final(
                @SerializedName("id")
                val id: Int,
                @SerializedName("title")
                val title: String,
                @SerializedName("message")
                val message: String,
                @SerializedName("status")
                val status: String,
                val date:String
            )
        }
    }
}
