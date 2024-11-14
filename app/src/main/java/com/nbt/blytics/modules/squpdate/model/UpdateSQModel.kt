package com.nbt.blytics.modules.squpdate.model
import com.google.gson.annotations.SerializedName


data class SqResponse(
    @SerializedName("question")
    val question: List<Question>,
    @SerializedName("status")
    val status: String,

) {
    data class Question(
        @SerializedName("id")
        val id: Int,
        @SerializedName("question")
        val question: String,
        var ans:String ="",
        var hint:String ="",
        var selected:Boolean = false
    ){
        override fun toString(): String {
            return question
        }
    }
}

data class UpdateSqlRequest(
    @SerializedName("que_data")
    val queData: List<QueData>,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("user_token")
    val userToken: String
) {
    data class QueData(
        @SerializedName("ques_no")
        var quesNo: String,
        @SerializedName("ans")
        var ans: String,
        @SerializedName("hint")
        var hint: String,
        @SerializedName("ques")
        var ques: Int
    )
}

data class UpdateSqResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message :String
)