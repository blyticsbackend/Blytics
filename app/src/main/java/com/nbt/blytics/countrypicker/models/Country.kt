package com.nbt.blytics.countrypicker.models

import android.graphics.Bitmap

/**
 * Created by Nbt on 07-07-2021
 */
data class Country(
    val name: String,
    val dial_code: String,
    val code: String,
    var flagId:Int
){
    override fun toString(): String {
        return "$name, $dial_code, $code"
    }
}