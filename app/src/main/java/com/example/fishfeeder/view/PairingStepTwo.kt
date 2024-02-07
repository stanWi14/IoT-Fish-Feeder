package com.example.fishfeeder.view

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.fishfeeder.R

class PairingStepTwo : Fragment() {
    private val locationPermissionCode = 1001
    private var connectedSSID: String? = null

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pairing_step_two, container, false)
        val connectButton: Button = view.findViewById(R.id.connect_button)
        val copyBtn: ImageButton = view.findViewById(R.id.btnCopyWifiPass)
        val nextButton: Button = view.findViewById(R.id.next_button)

        // Request location permission when the fragment is first opened
        checkLocationPermission()

        connectButton.setOnClickListener {
            openWifiSettings()
        }

        nextButton.setOnClickListener {
            // Check WiFi connection when the "Next" button is clicked
//            checkWifiConnection()
            // Check if connected to "FishFeeder" WiFi before proceeding to the next step
//            if (connectedSSID == "FishFeeder") {
                (requireActivity() as PairingActivity).loadFragment(PairingStepThree())
//            } else {
//                showToast("Not connected to FishFeeder WiFi")
//            }
        }

        copyBtn.setOnClickListener() {
            copyToClipBoard("P15HF3ED3rD3V1C3")
            Toast.makeText(requireContext(), "Passcode copied to clipboard", Toast.LENGTH_SHORT)
                .show()
        }

        return view
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Check and request location permission
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Location permission is already granted
                    checkWifiConnection()
                }
                else -> {
                    // Request location permission
                    ActivityCompat.requestPermissions(
                        requireContext() as Activity,
                        arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                        locationPermissionCode
                    )
                }
            }
        } else {
            // For devices running on versions lower than Android Q, you may handle the WiFi check without location permission
            checkWifiConnection()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkWifiConnection() {
        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        connectedSSID = getWifiSSID(connectivityManager)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getWifiSSID(connectivityManager: ConnectivityManager): String {
        val networkInfo = connectivityManager.activeNetworkInfo

        if (networkInfo != null && networkInfo.isConnected) {
            when (networkInfo.type) {
                ConnectivityManager.TYPE_WIFI -> {
                    val wifiInfo =
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)

                    if (wifiInfo != null && wifiInfo.isConnected) {
                        val wifiManager =
                            requireActivity().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                        val wifiInfo = wifiManager.connectionInfo
                        return wifiInfo.ssid?.removeSurrounding("\"") ?: "Unknown SSID"
                    }
                }
            }
        }

        return "No active WiFi connection"
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Location permission granted, check WiFi connection
                checkWifiConnection()
            } else {
                // Location permission denied, handle accordingly
                showToast("Location permission denied")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun copyToClipBoard(text: String) {
        val clipboardManager =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Text", text)
        clipboardManager.setPrimaryClip(clipData)
    }

    fun openWifiSettings() {
        (requireActivity() as PairingActivity).openWifiSettings()
    }
}