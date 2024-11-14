package com.nbt.blytics.modules.signupprofile.models
import com.google.gson.annotations.SerializedName


/**
 * Created bynbton 13-07-2021
 */
data class AvatarModel(
    val status: String,
    @SerializedName("data")
    val `data`: List<Data>?
) {
    data class Data(
        val id: Int,
        val image: String,
        var isSelect:Boolean =false
    )
}


data class LInkedAcRequest(
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("user_token")
    val userToken: String,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("address")
    val address: String,
    @SerializedName("pin_code")
    val pinCode: String,
    @SerializedName("country")
    val country: String,
    @SerializedName("state")
    val state: String,
    @SerializedName("user_phone")
    val userPhone: UserPhone,
    @SerializedName("relation")
    val relation: String,
    @SerializedName("buser")
    val buser: Boolean,
    @SerializedName("linked_user_id")
    val linkedUserId: String,
    @SerializedName("link_charge")
    val linkCharge: Boolean,
    @SerializedName("uuid")
    val uuid: String,
    @SerializedName("chargeable")
    val chargeable: Boolean
) {
    data class UserPhone(
        @SerializedName("mob_no")
        val mobNo: String,
        @SerializedName("country_code")
        val countryCode: String,
        @SerializedName("mob_verified")
        val mobVerified: String
    )
}

data class LinkedAccResponse(
    val status: String,
    val error_code:String,
    val message:String,
    val linkedUserId :Int,
    val bvnVerified:Boolean,
    @SerializedName("data")
    val `data`: Data?
){
    data class Data(
        @SerializedName("link_charge")
        val link_charge :Boolean,
        val chargeable:Boolean,
        val bvn_verified:Boolean,
        val bvn_number:String,
        val BVN_message:String,


    )
}

data class BvnCheck(
    val Bvn_number:String
)
data class BvnCheckResult(
    val status:String,
    val message:String,
)