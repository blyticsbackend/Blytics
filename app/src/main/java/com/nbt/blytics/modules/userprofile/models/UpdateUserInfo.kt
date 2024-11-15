package com.nbt.blytics.modules.userprofile.models

import com.google.gson.annotations.SerializedName



data class UpdateName(
    @SerializedName("user_id")
    val userid: String,
    @SerializedName("user_token")
    val userToken: String,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
)

data class UpdateDocument(
    @SerializedName("identity_proof_document")
    val idproof: String,
    @SerializedName("user_id")
    val userid: Int,
)

data class UpdateEmail(
    @SerializedName("user_id")
    val userid: String,
    @SerializedName("user_token")
    val userToken: String,
    val email: String,
    @SerializedName("email_verified")
    val emailVerified: Boolean
)

data class UpdateBVN(
    @SerializedName("user_id")
    val userid: String,
    @SerializedName("user_token")
    val userToken: String,
    val bvn: String,

)
data class UpdateMobile(
    @SerializedName("user_id")
    val userid: String,
    @SerializedName("user_token")
    val userToken: String,
    @SerializedName("mob_no")
    val mobNo: String,
    @SerializedName("mob_verified")
    val mobVerified: Boolean,
    val country_code:String=""
)

data class UpdateCountry(
    @SerializedName("user_id")
    val userid: String,
    @SerializedName("user_token")
    val userToken: String,
    val country: String,
    val state: String
)

data class UpdateState(
    @SerializedName("user_id")
    val userid: String,
    @SerializedName("user_token")
    val userToken: String,
    val state: String
)

data class UpdateDob(
    @SerializedName("user_id")
    val userid: String,
    @SerializedName("user_token")
    val userToken: String,
    @SerializedName("dob")
    val dob: String
)

data class UpdatePinCode(
    @SerializedName("user_id")
    val userid: String,
    @SerializedName("user_token")
    val userToken: String,
    @SerializedName("pin_code")
    val pincode: String
)
data class UpdateAddress(
    @SerializedName("user_id")
    val userid: String,
    @SerializedName("user_token")
    val userToken: String,
    @SerializedName("address")
    val address: String
)
data class UpdateProfileResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message: String,

)
data class UpdateDocumentResponse(
    @SerializedName("doc_verified")
    val status: String,

)

