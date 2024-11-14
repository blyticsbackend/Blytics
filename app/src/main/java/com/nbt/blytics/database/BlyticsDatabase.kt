package com.nbt.blytics.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(
    entities = [Contact::class], version = 2
)

abstract class BlyticsDatabase : RoomDatabase() {


    abstract fun getContactDao(): BlyticsDao

    companion object {
        const val DB_NAME = "contact_database"

        @Volatile
        private var INSTANCE: BlyticsDatabase? = null

        fun getInstance(context: Context): BlyticsDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BlyticsDatabase::class.java,
                    DB_NAME
                ).build()

                INSTANCE = instance
                return instance
            }
        }
    }


}
