package com.example.fishfeeder.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.fishfeeder.R

class PairingStepThree : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pairing_step_three, container, false)
        val etNewSSID: EditText = view.findViewById(R.id.edit_text1)
        val etNewPass: EditText = view.findViewById(R.id.edit_text2)
        val connectSSIDButton: Button = view.findViewById(R.id.connect_ssid_button)

        connectSSIDButton.setOnClickListener {
            val valueNewSSID = etNewSSID.text.toString()
            val valueNewPass = etNewPass.text.toString()
            assignWifiToEsp(valueNewSSID, valueNewPass)
        }
        return view
    }

    fun assignWifiToEsp(newSSID: String, newPass: String) {
        (requireActivity() as PairingActivity).assignWifiToEsp(newSSID, newPass)
    }
}