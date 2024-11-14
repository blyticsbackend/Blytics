package com.nbt.blytics.modules.sqverify.models

import com.google.gson.annotations.SerializedName


/**
 * Created bynbton 22-07-2021
 */
data class SQVerifyResponse(
    val status: String,
    @SerializedName("data")
    val `data`: Data? =null,
    @SerializedName("error_code")
    val errorCode: String = "",
    val message: String=""
) {
    data class Data(
        @SerializedName("ques_ans")
        val quesAns: List<QuesAn>
    ) {
        data class QuesAn(
            val ans: String,
            val hint: String,
            val ques: String,
            var userEnterAns:String=""
        )
    }
}

data class SQVerifyRequest(
    val user_id:String
)