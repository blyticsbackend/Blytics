package com.nbt.blytics.modules.payment.models
import com.google.gson.annotations.SerializedName


/**
 * Created by Nbt on 26-07-2021
 */
data class UserProfileInfoRequest(
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("user_token")
    val userToken: String
)

data class UserProfileInfoResponse(

    val status: String,
    @SerializedName("data")
    val `data`: Data? =null,
    val message:String="",
    @SerializedName("error_code")
    val errorCode:String=""
) {
    data class Data(
        val address: String,
        val avatar: Avatar,
        val bvn: String,
        @SerializedName("bvn_verified")
        val bvnVerified: Boolean,
        val country: String,
        @SerializedName("doc_verified")
        val docVerified: Boolean,
        val email: String,
        @SerializedName("email_verified")
        val emailVerified: Boolean,
        @SerializedName("mob_no")
        val mobNo: String,
        @SerializedName("mob_verified")
        val mobVerified: Boolean,
        @SerializedName("profile_status")
        val profileStatus: String,
        @SerializedName("user_id")
        val userId: String,
        @SerializedName("wallet_uuid")
        val walletUuid: String,
        @SerializedName("first_name")
        val firstName: String,
        @SerializedName("last_name")
        val lastName: String,
        @SerializedName("state")
        val state: String,
        @SerializedName("pincode")
        val pincode: String,
        @SerializedName("dob")
        val dob: String,
        @SerializedName("country_code")
        val countryCode: String,
        @SerializedName("security_question")
        val securityQuestion:List<SQ>
    ) {
        data class Avatar(
            @SerializedName("default_avatar")
            val defaultAvatar: String,
            @SerializedName("avatar_image")
            val avatarImage: String,
            val updated:String
        )
        data class SQ(
            val ques_no:String,
            val ans:String,
            val hint:String,
            val ques:String
        )
    }
}