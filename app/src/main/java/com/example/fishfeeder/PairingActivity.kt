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
import android.os.Handler
import android.provider.Settings
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.fishfeeder.databinding.ActivityPairingBinding
import com.example.fishfeeder.databinding.DialogNewWifiCredentialBinding
import com.example.fishfeeder.model.DeviceApplication
import com.google.firebase.firestore.FirebaseFirestore
import java.io.IOException
import java.io.PrintWriter
import java.net.Socket

class PairingActivity : AppCompatActivity() {
    companion object {
        private const val PERMISSION_REQUEST_CODE = 999
        private const val SECURITY_TYPE_WPA2 = "WPA2"
        private const val devSSID = "FishFeeder"
        private const val devPass = "12345678"
    }
    lateinit var binding: ActivityPairingBinding
    lateinit var deviceViewModel: DeviceViewModel
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var wifiManager: WifiManager
    private lateinit var wifiScanReceiver: BroadcastReceiver
    private var dialog: AlertDialog? = null

    val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPairingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // this is only for dummy data testing before fucntion developed
        deviceViewModel = ViewModelProvider(
            this,
            DeviceViewModelFactory((application as DeviceApplication).repository)
        ).get(DeviceViewModel::class.java)

        binding.btnConnect.setOnClickListener() {
            if(!isConnectedWifi("FishFeeder")){
                openWifiSettings()
            }
        }

        binding.btnPair.setOnClickListener() {
            assignWifiToEsp("Stanley123")
        }
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
    }

    private inner class SendDataToESP32 : AsyncTask<String, Void, Void>() {
        override fun doInBackground(vararg params: String): Void? {
            val message = params[0]
            try {
                val esp32IP = "192.168.4.1"
                val esp32Port = 80

                val socket = Socket(esp32IP, esp32Port)
                val out = PrintWriter(socket.getOutputStream(), true)

                out.print(message)
                out.flush()
                out.close()
                socket.close()

            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            // Show success dialog after sending data
        }
    }


    fun isWifiConnectedToSSID(context: Context, targetSSID: String): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Check if the device is connected to any network
        val network = connectivityManager.activeNetwork
        if (network == null) {
            return false
        }

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

    private fun openWifiSettings() {
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
        startActivity(intent)
    }

    private fun assignWifiToEsp(userId: String) {
        val dialogViewBinding = DialogNewWifiCredentialBinding.inflate(LayoutInflater.from(this))
        val dialogView = dialogViewBinding.root

        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)

        val alertDialog = dialogBuilder.create()

        dialogViewBinding.btnSubmit.setOnClickListener {
            val handler = Handler()
            var tryCount = 0
            val newSSID = dialogViewBinding.etNewSSID.text.toString()
            val newPass = dialogViewBinding.etNewPass.text.toString()
            val ownerUID = userId
            Toast.makeText(this, newSSID, Toast.LENGTH_SHORT).show()
            Toast.makeText(this, newPass, Toast.LENGTH_SHORT).show()
            Toast.makeText(this, ownerUID, Toast.LENGTH_SHORT).show()
            val combinedString = "$newSSID#$newPass#$ownerUID"
            SendDataToESP32().execute(combinedString)
            alertDialog.dismiss()
            showDialog("Connecting")
            val runnable = object : Runnable {
                override fun run() {
                    val isConnected = isConnectedWifi("FishFeeder")

                    if (isConnected) {
                        if (tryCount < 4) {
                            tryCount++
                            handler.postDelayed(this, 1000) // Retry after 1 second
                        } else {
                            closeDialog()
                            showDialog("Failed to Connect to Inputted Wifi")
                        }
                    } else {
                        // Connection failed
                        closeDialog()
                        showDialog("Successfullly assigned")
                    }
                }
            }

            // Start the initial attempt
            handler.post(runnable)
        }
        alertDialog.show()


    }

    private fun isConnectedWifi(targetSSID:String):Boolean{
        return if(isWifiConnectedToSSID(this, targetSSID)) {
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