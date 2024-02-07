package com.example.fishfeeder.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import com.example.fishfeeder.R

class PairingStepOne : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pairing_step_one, container, false)
        val nextButton: Button = view.findViewById(R.id.next_button)
        val cb:CheckBox = view.findViewById(R.id.cbBlinking)

        cb.isChecked = false

        // Disable button initially
        nextButton.isEnabled = false
        nextButton.alpha = 0.5f // You c
        cb.setOnCheckedChangeListener { _, isChecked ->
            // Enable or disable the button based on the checkbox state
            nextButton.isEnabled = isChecked

            // Change the color of the button if disabled
            if (isChecked) {
                nextButton.alpha = 1.0f
            } else {
                nextButton.alpha = 0.5f
            }
        }

        nextButton.setOnClickListener {
            (requireActivity() as PairingActivity).loadFragment(PairingStepTwo())
        }

        return view
    }
}