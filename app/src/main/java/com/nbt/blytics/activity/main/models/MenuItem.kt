package com.nbt.blytics.activity.main.models

data class MenuItem(
    val id: Int,
    val label: String,
    val image: Int,
    val destinationId: Int,
    var count: Int = 0,
    var isPin: Boolean = false,
    var pinPosition: Int = -1,
    var name: Name = Name.OTHER
) {
    override fun toString(): String {
        return label
    }
    enum class Name(str: String) {
        ACCOUNT("account"),
        LONE("lone"),
        CARD("card"),
        TRANSACTION("transaction"),
        SETTING("setting"),
        PAYEE("payee"),
        APPS("apps"),
        OTHER("other")
    }
}