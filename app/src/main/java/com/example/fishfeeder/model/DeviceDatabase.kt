package com.example.fishfeeder.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope

@Database(entities = [Device::class], version = 1)
abstract class DeviceDatabase : RoomDatabase() {

    abstract fun getDeviceDao(): DeviceDao

    //singleton structure
    companion object {
        @Volatile
        private var INSTANCE: DeviceDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): DeviceDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DeviceDatabase::class.java,
                    "table_device"
                )
                    .addCallback(DeviceDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }

    private class DeviceDatabaseCallback(private val scope: CoroutineScope) :
        RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
        }
    }

}