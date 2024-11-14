package com.nbt.blytics.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nbt.blytics.database.Contact.Companion.CONTACT_TABLE

@Entity(tableName = CONTACT_TABLE)
data class Contact(
    val contactId: Long,
    val name: String,
    val phoneNumber: String,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
) {
    companion object {
        const val   CONTACT_TABLE = "contact"
    }

}

