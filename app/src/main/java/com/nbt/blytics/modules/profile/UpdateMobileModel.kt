package com.nbt.blytics.modules.profile


data class UpdateMobileRequest(
    val user_id:String,
    val user_token:String,
    val mob_no:String,
    val mob_verified:Boolean,
    val device_token:String,
    val country_code:String
)

data class UpdateMobileResponse(
    val status:String,
    val message:String
)