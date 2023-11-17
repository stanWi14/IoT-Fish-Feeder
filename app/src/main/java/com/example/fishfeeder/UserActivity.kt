package com.example.fishfeeder

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.fishfeeder.databinding.ActivityMainBinding
import com.example.fishfeeder.databinding.ActivityUserBinding
import com.google.firebase.auth.FirebaseAuth

class UserActivity : AppCompatActivity() {

    lateinit var binding:ActivityUserBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid
        binding.txtSubTitle.setText(currentUser.toString())
    }
}