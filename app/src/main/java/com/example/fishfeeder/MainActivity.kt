package com.example.fishfeeder

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fishfeeder.databinding.ActivityMainBinding
import com.example.fishfeeder.databinding.DialogAddDeviceBinding
import com.example.fishfeeder.databinding.DialogUserProfileBinding
import com.example.fishfeeder.model.DeviceApplication
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    var isLogin: Boolean = false
    lateinit var deviceViewModel: DeviceViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        isLogin = loginState()

        if (!isLogin) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }
        //Device list part
        val recyclerView: RecyclerView = binding.rvListDevice
        recyclerView.layoutManager = LinearLayoutManager(this)
        val devAdapter = DeviceAdapter(this)
        recyclerView.adapter = devAdapter
        val viewModelFactory = DeviceViewModelFactory((application as DeviceApplication).repository)
        deviceViewModel = ViewModelProvider(this, viewModelFactory).get(DeviceViewModel::class.java)
        deviceViewModel.myDevices.observe(this, Observer { devices ->
            //update UI
            devAdapter.setDevice(devices)
        })

        binding.btnUserAcc.setOnClickListener() {
            showProfileDialog()
        }

        binding.btnAddDevice.setOnClickListener() {
            showAddDialog()
        }
    }

    fun loginState(): Boolean {
        val sharedPreferences = getSharedPreferences("LoginStatus", Context.MODE_PRIVATE)

        // Check if the app is being opened for the first time or after a fresh installation
        val isFirstRun = sharedPreferences.getBoolean("isFirstRun", true)

        if (isFirstRun) {
            // Set the flag to false to indicate that the app has been opened
            val editor = sharedPreferences.edit()
            editor.putBoolean("isFirstRun", false)
            editor.apply()

            // Return false to indicate that the user needs to log in
            return false
        }

        // Return the actual login status
        val isItReallyLogIn = sharedPreferences.getBoolean("isLoggedIn", false)
        return isItReallyLogIn
    }

    private fun showAddDialog() {
        val dialogViewBinding = DialogAddDeviceBinding.inflate(LayoutInflater.from(this))
        val dialogView = dialogViewBinding.root

        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)

        val alertDialog = dialogBuilder.create()

        dialogViewBinding.btnOwner.setOnClickListener {
            val intent = Intent(this, PairingActivity::class.java)
            startActivity(intent)
            alertDialog.dismiss()
        }
        dialogViewBinding.btnViewer.setOnClickListener {
            val intent = Intent(this, SubscribeActivity::class.java)
            startActivity(intent)
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun showProfileDialog() {
        val currentUID: String = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email.toString()
        val dialogViewBinding = DialogUserProfileBinding.inflate(LayoutInflater.from(this))
        val dialogView = dialogViewBinding.root

        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)

        val alertDialog = dialogBuilder.create()

        dialogViewBinding.txtDeviceID.setText("UID: $currentUID")
        dialogViewBinding.txtEmail.setText("Email: $currentUserEmail")

        dialogViewBinding.btnLogOut.setOnClickListener {
            clearSharedPreferences("isLoggedIn", "LoginStatus", "MyPrefs", "DevNum", "device")
            FirebaseAuth.getInstance().signOut()
            deviceViewModel.deleteAllDevices()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            alertDialog.dismiss()
            finish()
        }
        alertDialog.show()
    }

    fun clearSharedPreferences(vararg prefs: String) {
        prefs.forEach { prefName ->
            val sharedPreferences = getSharedPreferences(prefName, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()
        }
    }

}