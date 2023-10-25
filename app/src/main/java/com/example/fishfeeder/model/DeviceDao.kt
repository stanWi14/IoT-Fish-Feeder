package com.example.fishfeeder.model

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {
    @Insert
    fun insert(device: Device)

    @Update
    fun update(device: Device)

    @Delete
    fun delete(device: Device)

//    @Query("DELETE FROM device_table")
//    suspend fun deleteAllDevice()

    @Query("SELECT * FROM device_table")
    fun getAllDevice(): Flow<List<Device>>
}