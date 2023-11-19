package com.example.fishfeeder.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.fishfeeder.PairingActivity
import com.example.fishfeeder.R

class PairingStepTwo : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pairing_step_two, container, false)

        val connectButton: Button = view.findViewById(R.id.connect_button)
        val nextButton: Button = view.findViewById(R.id.next_button)

        connectButton.setOnClickListener {
            openWifiSettings()
        }

        nextButton.setOnClickListener {
            (requireActivity() as PairingActivity).loadFragment(PairingStepThree())
        }

        return view
    }

    fun openWifiSettings() {
        (requireActivity() as PairingActivity).openWifiSettings()
    }

}