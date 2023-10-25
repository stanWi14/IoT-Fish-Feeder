package com.example.fishfeeder.model

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class DeviceApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())
    val database by lazy {
        DeviceDatabase.getDatabase(this, applicationScope)
    }
    val repository by lazy {
        DeviceRepository(database.getDeviceDao())
    }
}