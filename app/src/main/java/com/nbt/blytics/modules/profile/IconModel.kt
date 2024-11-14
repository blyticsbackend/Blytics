package com.nbt.blytics.modules.profile

data class IconModel(
    val id:Int,
    val img:Int,
    val status:Status? =null
)
enum class Status(s :String){
     VERIFIED("verified"),
     PENDING("pending"),
     UNVERIFIED("unverified")
}