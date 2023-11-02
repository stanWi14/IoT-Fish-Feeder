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
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid


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

        binding.txtSubTitle.setOnClickListener() {
        }

        binding.btnAddDevice.setOnClickListener() {
            showCustomDialog()
        }
    }

    fun loginState(): Boolean {
        val sharedPreferences = getSharedPreferences("LoginStatus", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("isLoggedIn", false)
    }

    fun btnAddisClicked() {
        // show dialog
        // move according to choice
    }

    fun showConnectedDevice() {
        // read from local storage
        // show in recyclerview
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
        // Clear the login status in SharedPreferences (if you're using it)
        clearLoginStatus()

        // After signing out, you can redirect the user to the login screen or perform other actions
        // For example, you can navigate to the login activity.
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish() // Optional: Finish the current activity to prevent going back to the main activity
    }

    fun clearLoginStatus() {
        // shared preference is login true
        // pindah intent
        val sharedPreferences = getSharedPreferences("LoginStatus", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", false)
        editor.apply()
    }

    private fun showCustomDialog() {
        val dialogViewBinding = DialogAddDeviceBinding.inflate(LayoutInflater.from(this))
        val dialogView = dialogViewBinding.root

        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)

        val alertDialog = dialogBuilder.create()

        dialogViewBinding.btnOwner.setOnClickListener {
            val intent = Intent(this, PairingActivity::class.java)
            startActivity(intent)
        }
        dialogViewBinding.btnViewer.setOnClickListener {
            val intent = Intent(this, SubscribeActivity::class.java)
            startActivity(intent)
        }

        alertDialog.show()
    }
}