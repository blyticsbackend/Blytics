package com.nbt.blytics.modules.setpassword.models
import com.google.gson.annotations.SerializedName


/**
 * Created bynbton 21-07-2021
 */

data class AddAvatarResponse(

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
        val updated:String,
        @SerializedName("image_url")
        val imageUrl: String,
        @SerializedName("user_id")
        val userId: String
    )
}