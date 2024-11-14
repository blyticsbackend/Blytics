package com.nbt.blytics.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build.*
import android.os.Build.VERSION.RELEASE
import android.os.Build.VERSION.SDK_INT
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat

/**
 * Created bynbton 07-07-2021
 */
class DeviceInfoHelper (val context: Context) {

    val model = deviceModel

    val imei = context.imei

    val hardware: String? = HARDWARE

    val board: String? = BOARD

    val bootloader: String? = BOOTLOADER

    val user: String? = USER

    val host: String? = HOST

    val version: String? = RELEASE

    val apiLevel = SDK_INT

        val id: String? = ID

    val time = TIME

    val fingerPrint: String? = FINGERPRINT

    val display: String? = DISPLAY
    val DEFAULT_DEVICE_ID =0



    private val deviceModel
        @SuppressLint("DefaultLocale")
        get() = capitalize(
            if (MODEL.toLowerCase().startsWith(MANUFACTURER.toLowerCase())) {
                MODEL
            } else {
                "$MANUFACTURER $MODEL"
            })


    private fun capitalize(str: String) = str.apply {
        if (isNotEmpty()) {
            first().run { if (isLowerCase()) toUpperCase() }
        }
    }

    private val Context.imei
        @SuppressLint("HardwareIds", "MissingPermission")
        get() = telephonyManager?.run {
            if (isReadPhoneStatePermissionGranted()) {
                if (SDK_INT >= VERSION_CODES.O) {
                    imei
                } else {
                    deviceId
                }
            } else DEFAULT_DEVICE_ID
        } ?: DEFAULT_DEVICE_ID

    private fun Context.isReadPhoneStatePermissionGranted() =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED

    private val Context.telephonyManager
        get() = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?
}
