package com.example.fishfeeder.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "table_device")
data class Device(
    val devID: String,
    var titleDev: String,
    var beforeFeedVol: Double?,
    var afterFeedVol: Double?,
    var lastFeedTimeStamp: String,
    var allowNotif: Boolean,
) {
    @PrimaryKey(autoGenerate = true)
    var devNum = 0
}
