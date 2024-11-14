package com.nbt.blytics.common

enum class ComingFor(str: String) {
    FORGET_PASSWORD("forget_password"),
    SIGN_UP("sign_up"),
    LOGIN_MOBILE("login_mobile"),
    MOBILE_VERIFY("mobile_verity"),
    BACCOUNT("baccount"),
    SQ("sq"),
    CHANGE_TPIN("change_tpin"),
    CREATE_AC("create_ac"),
    ALL_AC("all_account"),
    HOME("home"),
    PROFILE("profile"),
    APPS("apps"),
    SECURITY_QUES("security_ques"),
    INFO("info"),
    CONTACT("contact"),
    PAY_NOW("pay_now"),
    TRANSACTION_HISTORY("transaction_history"),
    TRANSACTION_DETAILS("transaction_details"),
    PAY_AMOUNT("pay_amount"),
    AC_TO_BANK("acc_to_bank"),
    LINK_AC_PHONE_VERIFY("link_ac_phone_verify"),
    SELF_TRANSACTION("self_transaction"),
    NOTIFICATION_LIST("notification_list"),
    MANAGE_AC("manage_ac"),
    ACCOUNT_DETAILS("account_details"),
    TRANSACTION_HOME("transaction_home"),
    SETTING_HOME("setting_home"),
    CARD_HOME("card_home"),
    APPS_HOME("apps_home"),
    PAYEE_HOME("payee_home"),
    LONE_HOME("lone_home"),
    CREATE_SCHEDULE("create_schedule"),
    SET_SCHEDULE ("set_schedule"),
    MANAGE_LINK_AC("mange_link_ac")


}

enum class CheckFor(n: Int) {
    CHECK_STATE(0),
    FULL_DETAILS(1),
    FULL_DETAILS_LINKED(2)
}

enum class ComeBack(str: String) {
    PAYMENT_FRAGMENT("payment")
}

