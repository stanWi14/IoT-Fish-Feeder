package com.example.fishfeeder

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.fishfeeder.databinding.ActivityKosonganBinding

class Kosongan : AppCompatActivity() {
    // ini class untuk testing & show value
    // saat ini masih digunakan untuk show all list ( for notification )
    lateinit var binding: ActivityKosonganBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKosonganBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        displayPreferences()
    }

    private fun displayPreferences() {
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        val mondaySet = sharedPreferences.getStringSet("Monday", HashSet())
        val tuesdaySet = sharedPreferences.getStringSet("Tuesday", HashSet())
        val wednesdaySet = sharedPreferences.getStringSet("Wednesday", HashSet())
        val thursdaySet = sharedPreferences.getStringSet("Thursday", HashSet())
        val fridaySet = sharedPreferences.getStringSet("Friday", HashSet())
        val saturdaySet = sharedPreferences.getStringSet("Saturday", HashSet())
        val sundaySet = sharedPreferences.getStringSet("Sunday", HashSet())

        val stringBuilder = StringBuilder()

        stringBuilder.append("Monday: ").append(mondaySet?.joinToString(", ")).append("\n")
        stringBuilder.append("Tuesday: ").append(tuesdaySet?.joinToString(", ")).append("\n")
        stringBuilder.append("Wednesday: ").append(wednesdaySet?.joinToString(", ")).append("\n")
        stringBuilder.append("Thursday: ").append(thursdaySet?.joinToString(", ")).append("\n")
        stringBuilder.append("Friday: ").append(fridaySet?.joinToString(", ")).append("\n")
        stringBuilder.append("Saturday: ").append(saturdaySet?.joinToString(", ")).append("\n")
        stringBuilder.append("Sunday: ").append(sundaySet?.joinToString(", "))

        binding.txtFeed.text = stringBuilder.toString()
    }
}