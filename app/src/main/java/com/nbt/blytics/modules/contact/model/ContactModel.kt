package com.nbt.blytics.modules.contact.model

import android.net.Uri

/**
 * Created bynbton 26-10-2021
 */

data class ContactData(val contactId: Long, val name: String, val phoneNumber: List<String>, val avatar: Uri?) {
    override fun toString(): String {
        return "$name, $phoneNumber"
    }
}