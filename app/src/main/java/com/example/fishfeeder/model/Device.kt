package com.example.fishfeeder.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "device_table")
data class Device(
    val devID:String,
    val titleDev: String,
    val idDev: String,
    val beforeFeedVol: Int,
    val afterFeedVol: Int,
    val lastFeedTimeStamp:String,
    val allowNotif:Boolean,
){
    @PrimaryKey()
    var devNum = 0
}
