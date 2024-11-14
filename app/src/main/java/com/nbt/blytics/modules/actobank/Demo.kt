package com.nbt.blytics.modules.actobank



data class Demo(
    val eUser: Euser,
    val amount: String,
    val currency: String,
    val reference: String,
    val tpin_verified: Boolean,
    val user_id: Int,
    val user_token: String,
    val uuid: String
) {
    data class Euser(
        val destbankcode: String,
        val destbankname: String,
        val recipientaccount: String,
        val user_name: String
    )
}