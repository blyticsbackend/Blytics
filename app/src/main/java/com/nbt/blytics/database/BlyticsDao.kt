package com.nbt.blytics.database

import androidx.room.*

/**
 * Created bynbton 28-10-2021
 */
@Dao
interface BlyticsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insertContact(vararg contactData: Contact)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insertAllContact(contactData: MutableList<Contact>)

    @Delete
     fun deleteContact(vararg contactData: Contact)

    @Query("SELECT * FROM contact")
    fun getAllContact():List<Contact>

    @Query("DELETE FROM contact")
    fun clearAllContacts()
}