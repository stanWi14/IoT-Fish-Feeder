package com.example.fishfeeder

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fishfeeder.databinding.ActivityEditBinding
import com.example.fishfeeder.databinding.DialogAddScheduleBinding
import com.example.fishfeeder.model.Device
import com.example.fishfeeder.model.DeviceApplication
import com.google.firebase.firestore.FirebaseFirestore

class EditActivity : AppCompatActivity() {
    lateinit var binding: ActivityEditBinding
    lateinit var deviceViewModel: DeviceViewModel
    lateinit var selectedDevice: Device
    val db = FirebaseFirestore.getInstance()
    var monArray = mutableListOf<String>()
    var tueArray = mutableListOf<String>()
    var wedArray = mutableListOf<String>()
    var thuArray = mutableListOf<String>()
    var friArray = mutableListOf<String>()
    var satArray = mutableListOf<String>()
    var sunArray = mutableListOf<String>()
    lateinit var devId: String
    lateinit var devTitle: String
    var devANotifVal: Boolean = false
    var userStatus: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        val devNum: String? = intent.getStringExtra("DevNum")
        val devNumber: Int = devNum?.toInt() ?: 0
        val device: Device = intent.getParcelableExtra("device")!!
        devId = device.devID
        devTitle = device.titleDev
        devANotifVal = device.allowNotif
        userStatus = device.isOwner

        // Initialize deviceViewModel
        val viewModelFactory = DeviceViewModelFactory((application as DeviceApplication).repository)
        deviceViewModel = ViewModelProvider(this, viewModelFactory).get(DeviceViewModel::class.java)

        // Set The UI
        binding.txtValDevId.text = devId
        binding.etValDevTitle.setText(devTitle)
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
        readData(devId)
        binding.btnAddSchedule.setOnClickListener() {
            addScheduleDialog(devId)
        }
        binding.btnDisconnect.setOnClickListener() {
            Toast.makeText(this, devNumber.toString(), Toast.LENGTH_SHORT).show()
            device.devNum = devNumber
            deviceViewModel.delete(device)
            Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show()
            finish()
        }
        binding.btnUpdate.setOnClickListener() {
            val newTitle = binding.etValDevTitle.text.toString()
            val updatedDevice = Device(
                devID = device.devID,
                titleDev = newTitle,
                beforeFeedVol = device.beforeFeedVol,
                afterFeedVol = device.afterFeedVol,
                lastFeedTimeStamp = device.lastFeedTimeStamp,
                allowNotif = device.allowNotif,
                isOwner = device.isOwner
            )
            updatedDevice.devNum = devNumber
            deviceViewModel.update(updatedDevice)
            Toast.makeText(this, "Updated & Saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun addScheduleDialog(devId: String) {
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
                    // Add day to Local Array
                    when (day) {
                        "Monday" -> monArray.add(combinedString)
                        "Tuesday" -> tueArray.add(combinedString)
                        "Wednesday" -> wedArray.add(combinedString)
                        "Thursday" -> thuArray.add(combinedString)
                        "Friday" -> friArray.add(combinedString)
                        "Saturday" -> satArray.add(combinedString)
                        "Sunday" -> sunArray.add(combinedString)
                    }
                    saveToSharedPreferences(this, devId)
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
            showAllSchedule()
        }
        alertDialog.show()
    }

    fun readData(devId: String) {
        // Get data from Firebase
        db.collection("Schedules")
            .document(devId) // Specify the document ID
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null && document.exists()) {
                        monArray = document.get("Monday") as MutableList<String>
                        tueArray = document.get("Tuesday") as MutableList<String>
                        wedArray = document.get("Wednesday") as MutableList<String>
                        thuArray = document.get("Thursday") as MutableList<String>
                        friArray = document.get("Friday") as MutableList<String>
                        satArray = document.get("Saturday") as MutableList<String>
                        sunArray = document.get("Sunday") as MutableList<String>
                        saveToSharedPreferences(this, devId)
                        showAllSchedule()
                    } else {
                        // Handle the case where the document with the specified ID doesn't exist
                    }
                } else {
                    // Handle errors, e.g., network issues or Firestore security rules violation
//                    binding.txtListSchedule.setText("Failed to read data: ${task.exception?.message}")
                }
            }
    }

    private fun saveToSharedPreferences(context: Context, devId: String) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(devId, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        // Save schedule data for each day
        editor.putStringSet("Monday", HashSet(monArray))
        editor.putStringSet("Tuesday", HashSet(tueArray))
        editor.putStringSet("Wednesday", HashSet(wedArray))
        editor.putStringSet("Thursday", HashSet(thuArray))
        editor.putStringSet("Friday", HashSet(friArray))
        editor.putStringSet("Saturday", HashSet(satArray))
        editor.putStringSet("Sunday", HashSet(sunArray))
        editor.apply()
    }

    private fun deleteAllSchedules(context: Context) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        // Remove all schedules for each day
        editor.remove("Monday")
        editor.remove("Tuesday")
        editor.remove("Wednesday")
        editor.remove("Thursday")
        editor.remove("Friday")
        editor.remove("Saturday")
        editor.remove("Sunday")

        editor.apply()
    }

    fun showAllSchedule() {
        val recyclerView: RecyclerView = binding.rvSchedule
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Assuming you have fetched data into your arrays
        val monAdapter = ScheduleAdapter(monArray, this, devId, "Monday")
        val tueAdapter = ScheduleAdapter(tueArray, this, devId, "Tuesday")
        val wedAdapter = ScheduleAdapter(wedArray, this, devId, "Wednesday")
        val thuAdapter = ScheduleAdapter(thuArray, this, devId, "Thursday")
        val friAdapter = ScheduleAdapter(friArray, this, devId, "Friday")
        val satAdapter = ScheduleAdapter(satArray, this, devId, "Saturday")
        val sunAdapter = ScheduleAdapter(sunArray, this, devId, "Sunday")

        val adapters = listOf(
            monAdapter,
            tueAdapter,
            wedAdapter,
            thuAdapter,
            friAdapter,
            satAdapter,
            sunAdapter
        )

        val mergedAdapter = ConcatAdapter(adapters)
        recyclerView.adapter = mergedAdapter
    }

}