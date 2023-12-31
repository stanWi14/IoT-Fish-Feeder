package com.example.fishfeeder

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import java.util.Calendar
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
import com.google.firebase.firestore.SetOptions
import java.text.DecimalFormat

class EditActivity : AppCompatActivity() {
    lateinit var binding: ActivityEditBinding
    lateinit var deviceViewModel: DeviceViewModel
    val db = FirebaseFirestore.getInstance()
    var monArray = mutableListOf<String>()
    var tueArray = mutableListOf<String>()
    var wedArray = mutableListOf<String>()
    var thuArray = mutableListOf<String>()
    var friArray = mutableListOf<String>()
    var satArray = mutableListOf<String>()
    var sunArray = mutableListOf<String>()
    lateinit var devId: String
    var devPass: String? = null
    lateinit var devTitle: String
    lateinit var foodVal: String
    var devANotifVal: Boolean = false
    var userStatus: Boolean = false
    private lateinit var notificationHelper: NotificationHelper

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
        foodVal = DecimalFormat("#").format(device.minFoodVol).toString()

        // Initialize deviceViewModel
        val viewModelFactory = DeviceViewModelFactory((application as DeviceApplication).repository)
        deviceViewModel = ViewModelProvider(this, viewModelFactory).get(DeviceViewModel::class.java)

        // Set The UI
        // Set additional UI for device owner
        if (userStatus) {
            binding.btnAddSchedule.visibility = View.VISIBLE
            binding.linearDevPass.visibility = View.VISIBLE
            binding.etValFoodMin.visibility = View.VISIBLE
            binding.txtValFoodMin.visibility = View.GONE
            binding.etValFoodMin.setText(foodVal)
            binding.txtValPass.setOnClickListener() {
                readDeviceCloud(devId) { devPass ->
                    // Handle the devPass or null value here
                    if (devPass != null) {
                        binding.btnCopyPass.visibility = View.VISIBLE
                        binding.txtValPass.text = devPass
                        binding.btnCopyPass.setOnClickListener() {
                            copyToClipBoard(devPass)
                            Toast.makeText(this, "Passcode copied to clipboard", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        // Handle the case where the document doesn't exist or there was an error.
                        binding.txtValPass.text = "Failed to get passcode"
                    }
                }
            }
        }

        // Set text and saved preference data
        binding.txtValDevId.text = devId
        binding.etValDevTitle.setText(devTitle)
        binding.txtValFoodMin.text = foodVal
        binding.switchWarningNotif.isChecked = devANotifVal

        notificationHelper = NotificationHelper(this, devId, devNumber)
        (application as DeviceApplication).createNotificationChannel(devId)

        // Other existing code...

        binding.switchWarningNotif.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                assignAllSchedules()
                Toast.makeText(this, "Assigned", Toast.LENGTH_SHORT).show()
            } else {
                // When the switch is turned off, cancel all notifications for the current device
                notificationHelper.cancelAllNotifications()
            }
        }
        binding.btnCopyDevID.setOnClickListener() {
            copyToClipBoard(devId)
            Toast.makeText(this, "ID copied to clipboard", Toast.LENGTH_SHORT).show()
        }
        readScheduleCloud(devId)


        // UI button & firestore
        binding.btnAddSchedule.setOnClickListener() {
            addScheduleDialog(devId)
        }
        binding.btnDisconnect.setOnClickListener() {
            if (userStatus) {
                Toast.makeText(this, "Firestore delete all of the field", Toast.LENGTH_SHORT).show()
            }

            //delete device from local storage
            device.devNum = devNumber
            deleteAllSchedules(this)
            deviceViewModel.delete(device)
            Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show()
            finish()
        }
        binding.btnUpdate.setOnClickListener() {
            val newTitle = binding.etValDevTitle.text.toString()
            var newFoodVol = binding.etValFoodMin.text.toString()
            val newNotif = binding.switchWarningNotif.isChecked

            if (newTitle.isEmpty()) {
                // Display a warning toast
                Toast.makeText(this, "Can't update if empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (userStatus && newFoodVol != foodVal) {
                if (!TextUtils.isDigitsOnly(newFoodVol) || newFoodVol.isEmpty()) {
                    // Display a Toast message indicating that the input contains non-numeric characters
                    Toast.makeText(this, "incorrect food volume", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                Toast.makeText(this, "Firestore Updated", Toast.LENGTH_SHORT).show()
                val updates = hashMapOf<String, Any>(
                    "minFoodVol" to newFoodVol.toDouble()
                )

                db.collection("Devices").document(devId).update(updates).addOnFailureListener { e ->
                    Toast.makeText(
                        this, "Error updating Firestore: ${e.message}", Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                newFoodVol = foodVal
            }

            if (newTitle != device.titleDev || newFoodVol != foodVal || newNotif != device.allowNotif) {
                val updatedDevice = Device(
                    devID = device.devID,
                    titleDev = newTitle,
                    beforeFeedVol = device.beforeFeedVol,
                    afterFeedVol = device.afterFeedVol,
                    lastFeedTimeStamp = device.lastFeedTimeStamp,
                    allowNotif = newNotif,
                    isOwner = device.isOwner,
                    minFoodVol = newFoodVol.toDouble()
                )
                // update device to local storage
                updatedDevice.devNum = devNumber
                deviceViewModel.update(updatedDevice)
                Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "No Change Detected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun copyToClipBoard(text: String) {
        val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Text", text)
        clipboardManager.setPrimaryClip(clipData)
    }

    fun readScheduleCloud(devId: String) {
        // Get data from Firebase
        db.collection("Schedules").document(devId) // Specify the document ID as the device ID
            .get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null && document.exists()) {
                        monArray = document.get("Monday") as MutableList<String>?
                            ?: mutableListOf() // Use default value if null
                        tueArray =
                            document.get("Tuesday") as MutableList<String>? ?: mutableListOf()
                        wedArray =
                            document.get("Wednesday") as MutableList<String>? ?: mutableListOf()
                        thuArray =
                            document.get("Thursday") as MutableList<String>? ?: mutableListOf()
                        friArray = document.get("Friday") as MutableList<String>? ?: mutableListOf()
                        satArray =
                            document.get("Saturday") as MutableList<String>? ?: mutableListOf()
                        sunArray = document.get("Sunday") as MutableList<String>? ?: mutableListOf()
                        saveToSharedPreferences(this, devId)
                        showAllSchedule()
                    } else {
                        // Handle the case where the document with the specified ID doesn't exist
                    }
                } else {
                    // Handle errors, e.g., network issues or Firestore security rules violation
                    // binding.txtListSchedule.setText("Failed to read data: ${task.exception?.message}")
                }
            }
    }

    private fun addScheduleDialog(devId: String) {
        val dialogViewBinding = DialogAddScheduleBinding.inflate(LayoutInflater.from(this))
        val dialogView = dialogViewBinding.root

        val dialogBuilder = AlertDialog.Builder(this).setView(dialogView)

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
                }
            }

            // Update the Firestore document with the updated arrays
            val fieldUpdate = mapOf(
                "Monday" to monArray,
                "Tuesday" to tueArray,
                "Wednesday" to wedArray,
                "Thursday" to thuArray,
                "Friday" to friArray,
                "Saturday" to satArray,
                "Sunday" to sunArray
            )

            db.collection("Schedules").document(devId).set(fieldUpdate, SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(
                        applicationContext, "Schedule Added", Toast.LENGTH_SHORT
                    ).show()
                }.addOnFailureListener { e ->
                    Toast.makeText(
                        applicationContext,
                        "Error updating schedule: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

            // Check if the switch is true before scheduling the notification
            if (binding.switchWarningNotif.isChecked) {
                // Schedule notification
                for ((day, checkBox) in dayToFieldMap) {
                    if (checkBox.isChecked) {
                        notificationHelper.scheduleNotification(
                            getDayOfWeek(day), combinedString, devId
                        )
                    }
                }
            }

            saveToSharedPreferences(this, devId)
            alertDialog.dismiss()
            showAllSchedule()
        }
        alertDialog.show()
    }


    private fun getDayOfWeek(day: String): Int {
        return when (day) {
            "Monday" -> Calendar.MONDAY
            "Tuesday" -> Calendar.TUESDAY
            "Wednesday" -> Calendar.WEDNESDAY
            "Thursday" -> Calendar.THURSDAY
            "Friday" -> Calendar.FRIDAY
            "Saturday" -> Calendar.SATURDAY
            "Sunday" -> Calendar.SUNDAY
            else -> Calendar.SUNDAY
        }
    }


    fun readDeviceCloud(devId: String, callback: (String?) -> Unit) {
        db.collection("Devices").document(devId).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document.exists()) {
                    val devPass = document.getString("devPass").toString()
                    callback(devPass)
                } else {
                    // If the document doesn't exist, you can handle it accordingly.
                    callback(null)
                }
            } else {
                // If the task is not successful, you can handle it accordingly.
                callback(null)
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
            monAdapter, tueAdapter, wedAdapter, thuAdapter, friAdapter, satAdapter, sunAdapter
        )

        val mergedAdapter = ConcatAdapter(adapters)
        recyclerView.adapter = mergedAdapter
    }

    private fun assignAllSchedules() {
        // Loop through the lists and schedule all notifications
        scheduleAllForDay(Calendar.MONDAY, monArray)
        scheduleAllForDay(Calendar.TUESDAY, tueArray)
        scheduleAllForDay(Calendar.WEDNESDAY, wedArray)
        scheduleAllForDay(Calendar.THURSDAY, thuArray)
        scheduleAllForDay(Calendar.FRIDAY, friArray)
        scheduleAllForDay(Calendar.SATURDAY, satArray)
        scheduleAllForDay(Calendar.SUNDAY, sunArray)
    }

    private fun scheduleAllForDay(day: Int, scheduleList: List<String>) {
        // Create a copy of the list to avoid ConcurrentModificationException
        val scheduleListCopy = ArrayList(scheduleList)

        // Loop through the copied list and schedule notifications
        for (time in scheduleListCopy) {
            notificationHelper.scheduleNotification(day, time, devId)
        }
    }

}