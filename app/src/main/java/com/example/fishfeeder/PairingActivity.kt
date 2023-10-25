package com.example.fishfeeder

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.fishfeeder.databinding.ActivityLoginBinding
import com.example.fishfeeder.databinding.ActivityPairingBinding

class PairingActivity : AppCompatActivity() {
    lateinit var binding: ActivityPairingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPairingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    fun showPairingTutorial(){
        // show all pairing step tutorial image
    }

    fun searchDeviceWifi(){
        // search device by wifi named "FishFeederDevice"
        // if found try to connect
        connectDevice()
        // else maybe use a time out code
    }

    fun connectDevice(){
        //connect with wifi device
        // if connected then
        assignUID()
    }

    fun assignUID(){
        // send user ID to device as owner
        // dunno how to do it yet
    }
}