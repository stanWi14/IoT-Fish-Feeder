package com.example.fishfeeder

import android.app.Dialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.example.fishfeeder.databinding.ActivityMainBinding
import com.example.fishfeeder.databinding.DialogAddDeviceBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    lateinit var binding:ActivityMainBinding
    var isLogin:Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        isLogin = loginState()
        if(!isLogin){
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid
        binding.txtSubTitle.setText(currentUser.toString())

        binding.txtSubTitle.setOnClickListener(){
            logout()
        }
        binding.btnAddDevice.setOnClickListener(){
            showCustomDialog()
        }
    }

    fun loginState(): Boolean {
        val sharedPreferences = getSharedPreferences("LoginStatus", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("isLoggedIn", false)
    }
    fun btnAddisClicked(){
        // show dialog
        // move according to choice
    }
    fun showConnectedDevice(){
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