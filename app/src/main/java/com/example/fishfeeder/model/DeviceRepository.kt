package com.example.fishfeeder.model

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class DeviceRepository(private val devDao: DeviceDao) {
    val myDev: Flow<List<Device>> = devDao.getAllDevice()

    @WorkerThread
    suspend fun insert(dev: Device) {
        devDao.insert(dev)
    }

    @WorkerThread
    suspend fun update(dev: Device) {
        devDao.update(dev)
    }

    @WorkerThread
    suspend fun delete(dev: Device) {
        devDao.delete(dev)
    }

//    @WorkerThread
//    suspend fun deleteAllDev() {
//        devDao.deleteAllDevice()
//    }
}