package com.example.fishfeeder

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.fishfeeder.databinding.ActivityEditBinding

class EditActivity : AppCompatActivity() {
    lateinit var binding: ActivityEditBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
//        val devID = intent.getStringExtra("DevID")
//        if (devID != null) {
//            // Use the receivedValue in your EditActivity
//        }
//        binding.txtSubTitle.setText(devID)
    }

    fun readAllFeedingSchedule() {
        // read all feeding schedule
        // assign it into arrays
    }

    fun saveLocalRoom() {
        // title, notification, minimum warning value
    }

    fun addSchedule() {
        // show schedule dialog
        // when submit clicked
        // check if ada yang nabrak
        // if ada then show error dialog
        // if g ada then add ke array
        // apa perlu saya sort ?
    }

    fun deleteSchedule() {
        // remove an array value by button & recyclerview ?
        // duno how to implement rn
    }

    fun updateFireStore() {
        // update schedule to firestore using now array
    }
}