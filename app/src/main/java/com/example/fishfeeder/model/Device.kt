package com.example.fishfeeder.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "table_device")
data class Device(
    val devID: String,
    var titleDev: String,
    var beforeFeedVol: Double?,
    var afterFeedVol: Double?,
    var lastFeedTimeStamp: String,
    var allowNotif: Boolean, // remove this later, not used in the device; add it to user settings
    var isOwner: Boolean,
    var minFoodVol :Double?
) : Parcelable {

    @PrimaryKey(autoGenerate = true)
    var devNum: Int = 0
    
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readString()!!,
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readValue(Double::class.java.classLoader) as? Double
        )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(devID)
        parcel.writeString(titleDev)
        parcel.writeValue(beforeFeedVol)
        parcel.writeValue(afterFeedVol)
        parcel.writeString(lastFeedTimeStamp)
        parcel.writeByte(if (allowNotif) 1 else 0)
        parcel.writeByte(if (isOwner) 1 else 0)
        parcel.writeValue(minFoodVol)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Device> {
        override fun createFromParcel(parcel: Parcel): Device {
            return Device(parcel)
        }

        override fun newArray(size: Int): Array<Device?> {
            return arrayOfNulls(size)
        }
    }
}