package com.example.fishfeeder.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.fishfeeder.PairingActivity
import com.example.fishfeeder.R

class PairingStepFour : Fragment() {
    lateinit var etTitle: EditText
    lateinit var addButton: Button
    lateinit var txtDevID: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pairing_step_four, container, false)
        etTitle = view.findViewById(R.id.final_edit_text)
        addButton = view.findViewById(R.id.add_button)
        txtDevID = view.findViewById(R.id.txtDeviceID)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val devID = arguments?.getString("readDeviceID")
        txtDevID.setText(devID.toString())
        addButton.setOnClickListener {
            val devTitle = etTitle.text.toString()
            addDevice(devID.toString(), devTitle)
        }
    }

    fun addDevice(devID: String, devTitle: String) {
        (requireActivity() as PairingActivity).addDevice(devID, devTitle)
    }

}