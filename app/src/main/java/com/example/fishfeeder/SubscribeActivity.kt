package com.example.fishfeeder

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.fishfeeder.databinding.ActivitySubscribeBinding
import com.example.fishfeeder.model.Device
import com.example.fishfeeder.model.DeviceApplication
import com.google.firebase.firestore.FirebaseFirestore


class SubscribeActivity : AppCompatActivity() {
    lateinit var binding: ActivitySubscribeBinding
    val db = FirebaseFirestore.getInstance()
    lateinit var devID: String
    lateinit var devPass: String
    var beforeFeedVol: Double? = -1.0
    var afterFeedVol: Double? = -1.0
    lateinit var deviceViewModel: DeviceViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubscribeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        binding.btnAddDev.setOnClickListener() {
            verifyDevice()
        }
        deviceViewModel = ViewModelProvider(
            this,
            DeviceViewModelFactory((application as DeviceApplication).repository)
        ).get(DeviceViewModel::class.java)
    }

    fun verifyDevice() {
        // search dev id in firestore & check the devPass
        // if correct than save to local storage room
        devID = binding.etAddDevId.text.toString()
        devPass = binding.etAddDevPass.text.toString()
        val deviceCollection = db.collection("Devices")
        deviceCollection.document(devID)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document.exists()) {
                        if (devPass == document.getString("devPass")) {
                            afterFeedVol = document.getDouble("afterFeedVol")
                            beforeFeedVol = document.getDouble("beforeFeedVol")
                            addDevice()
                            Toast.makeText(applicationContext, "Connected", Toast.LENGTH_LONG)
                                .show()
                        } else {
                            Toast.makeText(applicationContext, "Wrong Password", Toast.LENGTH_LONG)
                                .show()
                        }
                    } else {
                        Toast.makeText(applicationContext, "Device ID Not Found", Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }
    }

    fun addDevice() {
        val devTitle = binding.etAddDevTitle.text.toString()
        // save device to local storage room
        val dev = Device(devID, devTitle, beforeFeedVol, afterFeedVol, "not yet feed", false, false)
        addDeviceToDatabase(dev)
    }

    private fun addDeviceToDatabase(dev: Device) {
        deviceViewModel.insert(dev)
        Toast.makeText(applicationContext, "Device Added", Toast.LENGTH_LONG).show()
        finish() // Close the activity after adding the device
    }
}