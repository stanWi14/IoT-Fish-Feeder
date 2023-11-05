package com.example.fishfeeder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.fishfeeder.databinding.ActivityEditBinding
import com.example.fishfeeder.databinding.DialogAddScheduleBinding
import com.example.fishfeeder.model.Device
import com.google.firebase.firestore.FirebaseFirestore

class EditActivity : AppCompatActivity() {
    lateinit var binding: ActivityEditBinding
    lateinit var deviceViewModel: DeviceViewModel
    lateinit var selectedDevice: Device
    val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        val devId = intent.getStringExtra("DevId")
        val devTitle = intent.getStringExtra("DevTitle")
        val devANotifVal = intent.getBooleanExtra("DevAN", false)
        val userStatus = intent.getBooleanExtra("UserStat", false)

        //TO DO this later we need to read all ( Maybe need )
//        val devNum = intent.getStringExtra("DevNum")
//        val num:Int = devNum?.toInt()!!
//        deviceViewModel.findByDevNum(num).observe(this, { device ->
//            if (device != null) {
//                Toast.makeText(applicationContext, "Found", Toast.LENGTH_LONG).show()
//                // Use the 'device' object as needed
//            } else {
//                Toast.makeText(applicationContext, "Not Found", Toast.LENGTH_LONG).show()
//            }
//        })


        // Set The UI
        binding.txtValDevId.text = devId
        binding.etValDevTitle.setText(devTitle)
        binding.switchWarningNotif.isChecked = devANotifVal
        if (userStatus) {
            binding.btnAddSchedule.visibility = View.VISIBLE
            binding.layoutAdvanceSet.visibility = View.VISIBLE
            binding.btnAdvanceSet.setOnClickListener {
                binding.advanceContainer.visibility =
                    if (binding.advanceContainer.visibility == View.GONE) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
            }
        }
        binding.btnAddSchedule.setOnClickListener() {
            if (devId != null) {
                addScheduleDialog(devId)
            }
        }
//        binding.sl

    }

    fun readAllFeedingSchedule() {
        // read all feeding schedule
        // assign it into arrays
    }

    private fun addScheduleDialog(devId:String) {
        val dialogViewBinding = DialogAddScheduleBinding.inflate(LayoutInflater.from(this))
        val dialogView = dialogViewBinding.root

        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)

        val alertDialog = dialogBuilder.create()

        dialogViewBinding.btnAddSchedule.setOnClickListener {
            val hour = dialogViewBinding.timePicker.currentHour
            val minute = dialogViewBinding.timePicker.currentMinute
            val portion = dialogViewBinding.etFeedPortion.text.toString()
            val combinedString =
                "${hour.toString().padStart(2, '0')}${minute.toString().padStart(2, '0')}${portion}"
            Toast.makeText(applicationContext, combinedString, Toast.LENGTH_LONG).show()

            val dayToFieldMap = mapOf(
                "Monday" to dialogViewBinding.cbMon,
                "Tuesday" to dialogViewBinding.cbTue,
                "Wednesday" to dialogViewBinding.cbWed,
                "Thursday" to dialogViewBinding.cbThu,
                "Friday" to dialogViewBinding.cbFri,
                "Saturday" to dialogViewBinding.cbSat,
                "Sunday" to dialogViewBinding.cbSun
            )

            val updatedFields = mutableMapOf<String, Any>()

            for ((day, checkBox) in dayToFieldMap) {
                if (checkBox.isChecked) {
                    // Fetch the existing array from Firestore
                    db.collection("Schedules")
                        .document(devId)
                        .get()
                        .addOnSuccessListener { documentSnapshot ->
                            val existingArray =
                                documentSnapshot.get(day) as? List<String> ?: emptyList()

                            // Append the new schedule to the existing array
                            val updatedArray = existingArray.toMutableList()
                            updatedArray.add(combinedString)

                            // Update the Firestore field with the updated array
                            val fieldUpdate = mapOf(day to updatedArray)

                            db.collection("Schedules")
                                .document(devId)
                                .update(fieldUpdate)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        applicationContext,
                                        "$day Added",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        applicationContext,
                                        "Error updating $day: ${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        }
                }
            }

            alertDialog.dismiss()
        }
        alertDialog.show()
    }


    fun deleteSchedule() {
        // remove an array value by button & recyclerview ?
        // duno how to implement rn
    }

    fun updateFireStore() {
        // update schedule to firestore using now array
    }
}