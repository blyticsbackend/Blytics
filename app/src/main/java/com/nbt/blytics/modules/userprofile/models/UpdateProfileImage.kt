package com.nbt.blytics.modules.userprofile.models

import com.google.gson.annotations.SerializedName

/**
 * Created bynbton 27-07-2021
 */
data class UpdateAvatarResponse(

    val status: String,
    val message: String,
    @SerializedName("data")
    val `data`: Data?=null,
    @SerializedName("error_code")
    val error_code: String=""
) {
    data class Data(
        @SerializedName("avatar_default")
        val avatarDefault: String,
        @SerializedName("image_url")
        val imageUrl: String,
        val updated:String

    )
}

data class SQ(
    val ques_no:String,
    val ans:String,
    val hint:String,
    val ques:String
)

