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

    @Query("SELECT * FROM table_device")
    fun getAllDevice(): Flow<List<Device>>

    @Query("DELETE FROM table_device")
    fun deleteAllDevice()
}