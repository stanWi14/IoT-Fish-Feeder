package com.example.fishfeeder.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.fishfeeder.R

class PairingStepFour : Fragment() {
    lateinit var etTitle: EditText
    lateinit var addButton: Button
    lateinit var txtDevID: TextView
    lateinit var etPassCode: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pairing_step_four, container, false)
        etTitle = view.findViewById(R.id.final_edit_text)
        addButton = view.findViewById(R.id.add_button)
        txtDevID = view.findViewById(R.id.txtDeviceID)
        etPassCode = view.findViewById(R.id.etSetPasscode)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var isButtonClicked = false
        val devID = arguments?.getString("readDeviceID")
        txtDevID.text = devID.toString()

        addButton.setOnClickListener {
            if (!isButtonClicked) {
                val devTitle = etTitle.text.toString()
                val devPassCode = etPassCode.text.toString()

                if (devTitle.isEmpty() || devPassCode.isEmpty()) {
                    if (devTitle.isEmpty()) {
                        showToast("Title cannot be empty")
                    }
                    if (devPassCode.isEmpty()) {
                        showToast("Passcode cannot be empty")
                    }
                } else {
                    addDevice(devID.toString(), devTitle, devPassCode)
                }
                isButtonClicked = true
                addButton.postDelayed({
                    isButtonClicked = false
                }, 10000)
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    fun addDevice(devID: String, devTitle: String, devPass: String) {
        (requireActivity() as PairingActivity).addDevice(devID, devTitle, devPass)
    }

}