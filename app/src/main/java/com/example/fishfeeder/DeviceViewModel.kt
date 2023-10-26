package com.example.fishfeeder

import androidx.lifecycle.*
import com.example.fishfeeder.model.Device
import com.example.fishfeeder.model.DeviceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DeviceViewModel(private val repository: DeviceRepository) : ViewModel() {
    val myDevices: LiveData<List<Device>> = repository.myDev.asLiveData()
    fun insert(dev: Device) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(dev)
    }

    fun update(dev: Device) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(dev)
    }

    fun delete(dev: Device) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(dev)
    }

}

class DeviceViewModelFactory(private var repository: DeviceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeviceViewModel::class.java)) {
            return DeviceViewModel(repository) as T
        } else {
            throw IllegalArgumentException("Unknown View Model")
        }
    }
}