package com.example.fishfeeder

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.fishfeeder.databinding.ActivityLoginBinding
import com.example.fishfeeder.databinding.ActivitySubscribeBinding

class SubscribeActivity : AppCompatActivity() {
    lateinit var binding: ActivitySubscribeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubscribeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    fun verifyDevice() {
        // search dev id in firestore & check the devPass
        // if correct than save to local storage room
        addDevice()
    }

    fun addDevice() {
        // save device to local storage room
    }
}