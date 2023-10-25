package com.example.fishfeeder

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.fishfeeder.databinding.ActivityEditBinding
import com.example.fishfeeder.databinding.ActivityMainBinding

class EditActivity : AppCompatActivity() {
    lateinit var binding:ActivityEditBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    fun readAllFeedingSchedule(){
        // read all feeding schedule
        // assign it into arrays
    }

    fun saveLocalRoom(){
        // title, notification, minimum warning value
    }

    fun addSchedule(){
        // show schedule dialog
        // when submit clicked
        // check if ada yang nabrak
        // if ada then show error dialog
        // if g ada then add ke array
        // apa perlu saya sort ?
    }

    fun deleteSchedule(){
        // remove an array value by button & recyclerview ?
        // duno how to implement rn
    }

    fun updateFireStore(){
        // update schedule to firestore using now array
    }
}