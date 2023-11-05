package com.example.fishfeeder

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.fishfeeder.databinding.ActivityPairingBinding
import com.example.fishfeeder.model.Device
import com.example.fishfeeder.model.DeviceApplication
import com.google.firebase.firestore.FirebaseFirestore

class PairingActivity : AppCompatActivity() {
    lateinit var binding: ActivityPairingBinding
    lateinit var deviceViewModel: DeviceViewModel
    val db = FirebaseFirestore.getInstance()
    lateinit var devID: String
    lateinit var devTitle: String
    lateinit var devPass: String
    var beforeFeedVol: Double? = -1.0
    var afterFeedVol: Double? = -1.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPairingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        binding.btnPair.setOnClickListener() {
            addDevice()
        }
        deviceViewModel = ViewModelProvider(
            this,
            DeviceViewModelFactory((application as DeviceApplication).repository)
        ).get(DeviceViewModel::class.java)
    }

    fun addDevice() {
        // dummy device
        // save device to local storage room
        val dev = Device(
            "sGghkYSKOULVLNVLaIY8",
            "Ivan dev",
            beforeFeedVol,
            afterFeedVol,
            "not yet feed",
            true,
            true
        )
        addDeviceToDatabase(dev)
    }

    private fun addDeviceToDatabase(dev: Device) {
        deviceViewModel.insert(dev)
        Toast.makeText(applicationContext, "Device Added", Toast.LENGTH_LONG).show()
        finish() // Close the activity after adding the device
    }

    fun showPairingTutorial() {
        // show all pairing step tutorial image
    }

    fun searchDeviceWifi() {
        // search device by wifi named "FishFeederDevice"
        // if found try to connect
        connectDevice()
        // else maybe use a time out code
    }

    fun connectDevice() {
        //connect with wifi device
        // if connected then
        assignUID()
    }

    fun assignUID() {
        // send user ID to device as owner
        // dunno how to do it yet
    }
}