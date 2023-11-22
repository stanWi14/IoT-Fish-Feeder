package com.example.fishfeeder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.example.fishfeeder.databinding.ActivityPairingBinding
import com.example.fishfeeder.model.Device
import com.example.fishfeeder.model.DeviceApplication
import com.example.fishfeeder.view.PairingStepFour
import com.example.fishfeeder.view.PairingStepOne
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class PairingActivity : AppCompatActivity() {
    lateinit var binding: ActivityPairingBinding
    lateinit var deviceViewModel: DeviceViewModel
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var wifiManager: WifiManager
    private lateinit var wifiScanReceiver: BroadcastReceiver
    private var dialog: AlertDialog? = null
    val db = FirebaseFirestore.getInstance()
    lateinit var currentUser: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPairingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        currentUser = FirebaseAuth.getInstance().currentUser?.uid.toString()

        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager

        wifiScanReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
            }
        }
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        registerReceiver(wifiScanReceiver, intentFilter)

        deviceViewModel = ViewModelProvider(
            this,
            DeviceViewModelFactory((application as DeviceApplication).repository)
        ).get(DeviceViewModel::class.java)

        // Load the first fragment
        loadFragment(PairingStepOne())
    }

    fun loadFragment(fragment: Fragment) {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }

    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        if (currentFragment is PairingStepFour) {
            finish()
        } else {
            super.onBackPressed()
        }
    }

    private inner class SendDataToESP32 : AsyncTask<String, Void, String?>() {
        override fun doInBackground(vararg params: String): String? {
            val message = params[0]
            try {
                val esp32IP = "192.168.4.1"
                val esp32Port = 80

                Socket(esp32IP, esp32Port).use { socket ->
                    PrintWriter(socket.getOutputStream(), true).use { out ->
                        // Send the message to the ESP32
                        out.print(message)
                        out.flush()

                        // Read the response from the ESP32
                        BufferedReader(InputStreamReader(socket.getInputStream())).use { reader ->
                            return reader.readLine()
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            // Process the response as needed
            if (result != null) {
                Toast.makeText(
                    this@PairingActivity,
                    "Received response from ESP32: $result",
                    Toast.LENGTH_SHORT
                ).show()
                closeDialog()
                replaceFragmentAndSendData(result)
            } else {
                // Handle the case where no response is received
                Toast.makeText(
                    this@PairingActivity,
                    "No response received from ESP32",
                    Toast.LENGTH_SHORT
                ).show()
            }
            closeDialog()
            // Show success dialog after sending data
            // You may add additional logic or UI updates here if needed
        }

        override fun onCancelled() {
            super.onCancelled()
            // Handle the case where the AsyncTask is canceled
            Toast.makeText(this@PairingActivity, "AsyncTask canceled", Toast.LENGTH_SHORT).show()
            closeDialog()
        }
    }

    private fun replaceFragmentAndSendData(devID: String) {
        val addTitleFragment = PairingStepFour()

        // Pass data to the fragment using Bundle
        val bundle = Bundle()
        bundle.putString("readDeviceID", devID)
        addTitleFragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, addTitleFragment)
            .addToBackStack(null)
            .commit()
    }

    fun isWifiConnectedToSSID(context: Context, targetSSID: String): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Check if the device is connected to any network
        val network = connectivityManager.activeNetwork ?: return false

        val wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo: WifiInfo = wifiManager.connectionInfo

        // On Android 10 (API level 29) and above, use NetworkCapabilities to check if it's a Wi-Fi network
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
            if (networkCapabilities != null && networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return wifiInfo.ssid == "\"$targetSSID\""
            }
        } else {
            // On versions below Android 10, directly check the Wi-Fi SSID
            return wifiInfo.ssid == "\"$targetSSID\""
        }

        return false
    }

    fun openWifiSettings() {
//        if(!isConnectedWifi("FishFeeder")){
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
        startActivity(intent)
//        }
    }

    fun assignWifiToEsp(newSSID: String, newPass: String) {
        val ownerUID = currentUser
        Toast.makeText(this, newSSID, Toast.LENGTH_SHORT).show()
        Toast.makeText(this, newPass, Toast.LENGTH_SHORT).show()
        Toast.makeText(this, ownerUID, Toast.LENGTH_SHORT).show()
        val combinedString = "$newSSID#$newPass#$ownerUID"
        showDialog("Sending Data")
        val sendDataToESP32 = SendDataToESP32()
        sendDataToESP32.execute(combinedString)
    }

    fun addDevice(devID: String, devTitle: String) {
        val deviceCollection = db.collection("Devices")
        deviceCollection.document(devID)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document.exists()) {
                        val afterFeedVol = document.getDouble("afterFeedVol")
                        val beforeFeedVol = document.getDouble("beforeFeedVol")
                        val minFoodVol = document.getDouble("minFoodVol")
                        val dev = Device(
                            devID,
                            devTitle,
                            beforeFeedVol,
                            afterFeedVol,
                            "not yet feed",
                            false,
                            true,
                            minFoodVol
                        )
                        addDeviceToDatabase(dev)
                        Toast.makeText(applicationContext, "Device Added", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(applicationContext, "Device ID Not Found", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    private fun addDeviceToDatabase(dev: Device) {
        deviceViewModel.insert(dev)
        Toast.makeText(applicationContext, "Device Added", Toast.LENGTH_SHORT).show()
        finish() // Close the activity after adding the device
    }

    fun isConnectedWifi(targetSSID: String): Boolean {
        return if (isWifiConnectedToSSID(this, targetSSID)) {
            // Your device is connected to Wi-Fi with the specified SSID
            Toast.makeText(this, "Connected to FishFeeder Wi-Fi", Toast.LENGTH_SHORT).show()
            true
        } else {
            // Your device is not connected to Wi-Fi with the specified SSID
            Toast.makeText(this, "Not connected to FishFeeder Wi-Fi", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun showDialog(message: String) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Fish Feeder")
        dialogBuilder.setMessage(message)
        dialogBuilder.setCancelable(false)
        dialogBuilder.setPositiveButton("OK") { _, _ ->
            // Do nothing, just dismiss the dialog
            closeDialog()
        }
        dialog = dialogBuilder.create()
        dialog!!.show()
    }

    private fun closeDialog() {
        // This function will dismiss the currently displayed dialog
        if (dialog != null && dialog!!.isShowing) {
            dialog!!.dismiss()
        }
    }
}