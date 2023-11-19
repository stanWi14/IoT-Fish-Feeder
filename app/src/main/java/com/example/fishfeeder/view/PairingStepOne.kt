package com.example.fishfeeder.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.fishfeeder.PairingActivity
import com.example.fishfeeder.R

class PairingStepOne : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pairing_step_one, container, false)
        val nextButton: Button = view.findViewById(R.id.next_button)

        nextButton.setOnClickListener {
            (requireActivity() as PairingActivity).loadFragment(PairingStepTwo())
        }

        return view
    }
}