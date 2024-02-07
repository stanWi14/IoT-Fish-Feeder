package com.example.fishfeeder.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fishfeeder.control.DeviceViewModel
import com.example.fishfeeder.control.DeviceViewModelFactory
import com.example.fishfeeder.control.NotificationHelper
import com.example.fishfeeder.databinding.ActivityEditBinding
import com.example.fishfeeder.databinding.DialogAddScheduleBinding
import com.example.fishfeeder.model.Device
import com.example.fishfeeder.model.DeviceApplication
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.DecimalFormat
import java.util.*

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


        binding.switchWarningNotif.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                assignAllSchedules()
                Toast.makeText(this, "Assigned", Toast.LENGTH_SHORT).show()
            } else {
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
                Toast.makeText(this, "Can't update if empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (userStatus && newFoodVol != foodVal) {
                if (!TextUtils.isDigitsOnly(newFoodVol) || newFoodVol.isEmpty()) {
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
                    }
                }
            }
    }

    private fun addScheduleDialog(devId: String) {
        val dialogViewBinding = DialogAddScheduleBinding.inflate(LayoutInflater.from(this))
        val dialogView = dialogViewBinding.root

        val dialogBuilder = AlertDialog.Builder(this).setView(dialogView)

        val alertDialog = dialogBuilder.create()

        dialogViewBinding.btnAddSchedule.setOnClickListener {
            val dayToFieldMap = mapOf(
                "Monday" to dialogViewBinding.cbMon,
                "Tuesday" to dialogViewBinding.cbTue,
                "Wednesday" to dialogViewBinding.cbWed,
                "Thursday" to dialogViewBinding.cbThu,
                "Friday" to dialogViewBinding.cbFri,
                "Saturday" to dialogViewBinding.cbSat,
                "Sunday" to dialogViewBinding.cbSun
            )

            if (!dayToFieldMap.any { it.value.isChecked }) {
                Toast.makeText(
                    applicationContext,
                    "Please select at least one day.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val portion = dialogViewBinding.etFeedPortion.text.toString()
            if (portion.isEmpty()) {
                Toast.makeText(applicationContext, "Please enter feed portion.", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val hour = dialogViewBinding.timePicker.currentHour
            val minute = dialogViewBinding.timePicker.currentMinute
            if (hour == 0 && minute == 0) {
                Toast.makeText(
                    applicationContext,
                    "Please select a valid time.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val combinedString =
                "${hour.toString().padStart(2, '0')}${minute.toString().padStart(2, '0')}${portion}"


            for ((day, checkBox) in dayToFieldMap) {
                if (checkBox.isChecked) {
                    val combinedString =
                        "${hour.toString().padStart(2, '0')}${
                            minute.toString().padStart(2, '0')
                        }$portion"

                    if (scheduleExists(day, combinedString)) {
                        Toast.makeText(
                            applicationContext,
                            "Schedule already exists for $day.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }
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

            if (binding.switchWarningNotif.isChecked) {
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

    private fun scheduleExists(day: String, newSchedule: String): Boolean {
        val scheduleList = when (day) {
            "Monday" -> monArray
            "Tuesday" -> tueArray
            "Wednesday" -> wedArray
            "Thursday" -> thuArray
            "Friday" -> friArray
            "Saturday" -> satArray
            "Sunday" -> sunArray
            else -> emptyList()
        }
        return scheduleList.contains(newSchedule)
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
                    callback(null)
                }
            } else {
                callback(null)
            }
        }
    }

    private fun saveToSharedPreferences(context: Context, devId: String) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(devId, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
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

        val monAdapter =
            ScheduleAdapter(monArray.sortedBy { it.substring(0, 4) }, this, devId, "Monday")
        val tueAdapter =
            ScheduleAdapter(tueArray.sortedBy { it.substring(0, 4) }, this, devId, "Tuesday")
        val wedAdapter =
            ScheduleAdapter(wedArray.sortedBy { it.substring(0, 4) }, this, devId, "Wednesday")
        val thuAdapter =
            ScheduleAdapter(thuArray.sortedBy { it.substring(0, 4) }, this, devId, "Thursday")
        val friAdapter =
            ScheduleAdapter(friArray.sortedBy { it.substring(0, 4) }, this, devId, "Friday")
        val satAdapter =
            ScheduleAdapter(satArray.sortedBy { it.substring(0, 4) }, this, devId, "Saturday")
        val sunAdapter =
            ScheduleAdapter(sunArray.sortedBy { it.substring(0, 4) }, this, devId, "Sunday")

        val adapters = listOf(
            monAdapter, tueAdapter, wedAdapter, thuAdapter, friAdapter, satAdapter, sunAdapter
        )

        val mergedAdapter = ConcatAdapter(adapters)
        recyclerView.adapter = mergedAdapter
    }

    private fun assignAllSchedules() {
        scheduleAllForDay(Calendar.MONDAY, monArray)
        scheduleAllForDay(Calendar.TUESDAY, tueArray)
        scheduleAllForDay(Calendar.WEDNESDAY, wedArray)
        scheduleAllForDay(Calendar.THURSDAY, thuArray)
        scheduleAllForDay(Calendar.FRIDAY, friArray)
        scheduleAllForDay(Calendar.SATURDAY, satArray)
        scheduleAllForDay(Calendar.SUNDAY, sunArray)
    }

    private fun scheduleAllForDay(day: Int, scheduleList: List<String>) {
        val scheduleListCopy = ArrayList(scheduleList)
        for (time in scheduleListCopy) {
            notificationHelper.scheduleNotification(day, time, devId)
        }
    }

}