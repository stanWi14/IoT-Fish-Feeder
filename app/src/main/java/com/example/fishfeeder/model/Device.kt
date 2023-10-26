package com.example.fishfeeder.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "table_device")
data class Device(
    val devID: String,
    val titleDev: String,
    val beforeFeedVol: Double?,
    val afterFeedVol: Double?,
    val lastFeedTimeStamp: String,
    val allowNotif: Boolean,
) {
    @PrimaryKey()
    var devNum = 0
}
