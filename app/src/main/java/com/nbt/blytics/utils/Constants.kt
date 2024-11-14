package com.nbt.blytics.utils

import com.nbt.blytics.R
import java.util.*

object Constants {

    const val RECORD_REQUEST_CODE = 101
    const val CAMERA_ = 1
    const val GALLERY = 2
    const val PIC_CROP = 102
    const val COMING_FOR = "comingFor"
    const val ACC_TYPE="ac_type"
    const val RESULT_CODE="result_code"
    const val SELECTED_AC="selected_ac"
    const val USER_ID: String = "user_id"
    const val USER_WALLET_UUID: String = "user_wallet_uuid"
    const val PHONE_NUMBER: String = "phone_number"
    const val HAVE_TPIN ="have_tpin"
    const val HAVE_SECURITY_QUESTION ="have_security_question"
    const val USER_FIRST_NAME: String = "user_first_name"
    const val USER_LAST_NAME: String = "use_last_name"
    const val USER_DOB: String = "user_dob"
    const val USER_GENDER: String = "user_gender"
    const val USER_EMAIL: String = "user_email"
    const val USER_ADDRESS: String = "user_address"
    const val USER_COUNTRY: String = "user_country"
    const val USER_STATE: String = "user_state"
    const val USER_PIN_CODE: String = "user_pin_code"
    const val USER_PASSWORD: String = "user_password"
    const val CODE_AUTHENTICATION_VERIFICATION = 241
    const val SAVING_ACC = "saving_account"
    const val CURRENT_ACC = "current_account"
    const val WALLET_ACC = "wallet_account"
    const val LINKED_ACC = "linked_account"
    const val ALL_AC = "all_account"
    const val ACC_NUM = "account_number"
    const val ACC_PURPOSE= "account_purpose"
    const val TXN_HISTORY_TYPE="txn_history_type"
    const val SELF_TXN ="self_txn"
    const val PAY_MODE ="send_money"
    const val SHOW_QR ="show_qr"
    const val DEFAULT_CURRENCY ="₦"
    const val BANK_DUMMY_IMAGE ="https://img.icons8.com/dotty/80/000000/bank-building.png"

    enum class PayType(string: String){
        SENT_MONEY("send_money"),
        REQUEST_MONEY("request_money"),
        BOTH("both"),
        RECENT_PAYEE("recent_payee"),
        REQUESTER_SEND_MONEY("requester_send_money"),
        SCHEDULE("schedule")
    }
    enum class AnsYN(string: String){
        YES("Yes"),
        NO(("No"))
    }
    enum class TxnHistoryType(string: String) {
        DEBIT("Debit"),
        CREDIT("Credit")
    }

    var DEFAULT_COUNTRY_CODE:String = "+234"

    const val SPECIAL_CHAR = ".*[~!@#\$%\\^&*()\\-_=+\\|\\[{\\]};:'\",<.>/?0-9].*"

    const val PUSH_NOTIFICATION="push_notification"
    const val NOTIFICATION_TYPE ="notification_type"
    const val NOTIFICATION_TITLE ="notification_title"
    const val NOTIFICATION_INFO_BUNDLE ="notification_info_bundle"
    const val COMING_FROM_NOTIFICATION ="coming_from_notification"

    enum class Status(string: String) {
        SUCCESS("Success"),
        FAILED("Failed")
    }

    enum class UpdatedAvatar(string: String) {
        AVATAR_DEFAULT("avatar_default"),
        AVATAR_IMAGE("avatar_image")
    }


    fun errorMessage(errorCode: Int): String {
        return when (errorCode) {
            101 -> {
                "ERROR_VALUE_NOT_MATCH "
            }
            102 -> {
                "ERROR_NOT_EXIST "
            }
            103 -> {
                "ERROR_INVALID_DATA"
            }
            104 -> {
                "ERROR_INVALID_REQUEST"
            }
            105 -> {
                "ERROR_ERROR_UNKNOWN"
            }
            106 -> {
                "ERROR_USER_NOT_AUTHENTICATED"
            }
            107 -> {
                "ERROR_USER_BLOCKED"
            }
            108 -> {
                "ERROR_EXIST"
            }
            109 -> {
                "ERROR_PAYMENT_REQUIRED"
            }
            else -> ""
        }
    }


    const val AUTHENTICATION_FAILED = "Authentication failed"
    const val AUTHENTICATION_SUCCEEDED = "Authentication succeeded"
    const val AUTHENTICATION_ERROR = "Authentication error"

    const val BIOMETRIC_AUTHENTICATION = "Biometric Authentication"
    const val USE_DEVICE_PASSWORD = "Use device password"
    const val BIOMETRIC_AUTHENTICATION_SUBTITLE = "Use your fingerprint to authenticate"
    const val BIOMETRIC_AUTHENTICATION_DESCRIPTION = "This app uses your makes use of device biometrics (user fingerprint) to authenticate the dialog."

    const val AUTHENTICATE_OTHER="Authenticate using Device Password/PIN"

    const val AVAILABLE="Available"
    const val UNAVAILABLE="Unavailable"
    const val TRUE="True"
    const val FALSE="False"
    const val CANCEL="Cancel"

    const val PASSWORD_PIN_AUTHENTICATION = "Password/PIN Authentication"
    const val PASSWORD_PIN_AUTHENTICATION_SUBTITLE = "Authenticate using Device Password/PIN"
    const val PASSWORD_PIN_AUTHENTICATION_DESCRIPTION = "This app uses your makes use of device password/pin to authenticate the dialog."

}