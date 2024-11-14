package com.nbt.blytics.utils

import android.content.Context
import android.content.SharedPreferences

object SharePreferences {

    private lateinit var sharedPreferences: SharedPreferences
    private const val user_pref = "app_data_dfm"
    private lateinit var sharePreferences: SharePreferences

    const val  IS_TIMER_ACTIVATED ="is_timer_activated"
    const val   INITIAL_ACTIVE_TIME ="initial_active_time"

    const val IS_SECOND_TIME_OPEN_APP ="is_second_time_open"
    const val DEVICE_TOKEN="device_token"
    const val USER_TOKEN = "user_token"
    const val USER_ID = "user_id"
    const val USER_WALLET_UUID = "user_wallet_uuid"
    const val USER_LINKED_UUID = "user_linked_uuid"
    const val SYSTEM_LOCK_IS_ACTIVE ="system_lock_is_active"
    const val PASSWORD_LOCK_IS_ACTIVE ="password_lock_is_active"
    const val SMS_NOTIFICATION ="sms_notification"
    const val WHATSAPP_NOTIFICATION ="whatsapp_notification"
    const val EMAIL_NOTIFICATION ="default_notification"
    const val SYSTEM_MODE_DAY ="system_mode_day"
    const val SYSTEM_MODE_NIGHT ="system_mode_night"

    const val IS_MODE_SELECTED = "is_mode_selected"
    const val SELECTED_MODE = "selected_mode"

    const val SET_DEFAULT_APPS="set_default_apps"

    const val USER_MOBILE_NUMBER = "user_mobile_number"
    const val USER_MOBILE_VERIFIED = "user_mobile_verified"
    const val USER_EMAIL = "user_email"
    const val USER_EMAIL_VERIFIED = "user_email_verified"
    const val USER_ADDRESS = "user_address"
    const val USER_COUNTRY = "user_country"
    const val USER_BVN = "user_bvn"
    const val USER_BVN_VERIFIED = "user_bvn_verified"
    const val USER_DOC_VERIFIED = "user_doc_verified"
    const val USER_PROFILE_STATUS = "user_profile_status"
    const val USER_PROFILE_IMAGE = "user_profile_image"
    const val USER_SECURITY_QUES = "user_sq"

    const val USER_FIRST_NAME = "user_first_name"
    const val USER_LAST_NAME = "user_last_name"
    const val USER_DOB = "user_dob"
    const val USER_COUNTRY_CODE ="user_country_code"
    const val USER_STATE = "user_state"
    const val USER_PIN_CODE = "user_pin_code"

    const val APPS_MENU = "apps_menu"
    const val MAIN_MENU = "main_menu"
    const val DEFAULT_ACCOUNT = "default_account"
    const val DEFAULT_PURPOSE = "default_purpose"
    const val DEFAULT_AC_TYPE = "default_ac_type"


    const val IS_REMEMBER_PASSWORD = "is_remember_password"
    const val USER_LOGIN_EMAIL_MOBILE = "user_login_email_mobile"
    const val PRESENTATION_COMPLETED = "presentation_completed"


    enum class AcType(s:String){
        SAVING("saving"),
        CURRENT("current"),
        WALLET("wallet"),
        LINKED("linked")


    }
    private fun init(context: Context) {
        sharePreferences = SharePreferences
        sharedPreferences =
            context.getSharedPreferences(user_pref, Context.MODE_PRIVATE)
    }


    fun getInstance(context: Context): SharePreferences {
        if (SharePreferences::sharePreferences.isInitialized.not()) {
            init(context)
        }
        return sharePreferences
    }

    fun clearPreference() {
        val editor = getEditor()
        editor.clear()
        editor.apply()
    }

    fun setStringValue(key: String, value: String) {
        val editor = getEditor()
        editor.putString(key, value)
        editor.apply()
    }

    fun getStringValue(key: String, default: String): String {
        return sharedPreferences.getString(key, default)!!
    }

    fun setIntValue( key: String,value: Int) {
        val editor = getEditor()
        editor.putInt(key, value)
        editor.apply()
    }

    fun getIntValue(key: String): Int {
        return sharedPreferences.getInt(key, 0)
    }

    fun removeAllData() {
        val editor = getEditor()
        editor.clear()
        editor.apply()
    }

    private fun getEditor(): SharedPreferences.Editor {
        return sharedPreferences.edit()
    }

    fun getBooleanValue(key: String): Boolean {
        return sharedPreferences.getBoolean(key, false)
    }

    fun setBooleanValue(key: String, value: Boolean) {
        val editor = sharedPreferences.edit()
        editor?.putBoolean(key, value)
        editor?.apply()
    }


}
