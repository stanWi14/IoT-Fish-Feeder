package com.example.fishfeeder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class ScheduleAdapter(
    private val data: List<String>,
    private val activity: EditActivity,
    private val devId: String,
    private val dayOfWeek: String
) : RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    class ScheduleViewHolder(scheduleView: View) : RecyclerView.ViewHolder(scheduleView) {
        val labelDay: TextView = scheduleView.findViewById(R.id.txtDay)
        val labelTime: TextView = scheduleView.findViewById(R.id.txtTime)
        val labelPortion: TextView = scheduleView.findViewById(R.id.txtPortion)
        val btnDelSchedule: ImageButton = scheduleView.findViewById(R.id.btnDelSchedule)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ScheduleAdapter.ScheduleViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_schedule_card, parent, false)
        return ScheduleAdapter.ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        if (activity.userStatus) {
            holder.btnDelSchedule.visibility = View.VISIBLE
        }
        val currentFormat = data[position]
        val hour = currentFormat.substring(0, 2).toInt()
        val minute = if (currentFormat.length >= 4) currentFormat.substring(2, 4).toInt() else 0
        val portion = if (currentFormat.length > 4) currentFormat.substring(4) else ""

        holder.labelTime.text = "$hour:$minute"
        holder.labelPortion.text = portion
        holder.labelDay.text = dayOfWeek.substring(0, minOf(3, dayOfWeek.length))

        holder.btnDelSchedule.setOnClickListener() {
            deleteSchedule(currentFormat)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun deleteSchedule(deleteFormat: String) {
        val db = FirebaseFirestore.getInstance()
        val documentPath = "Schedules/$devId"
        val dayToRemoveValueFrom = dayOfWeek
        val valueToRemove = deleteFormat
        val docRef = db.document(documentPath)

        docRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val data = documentSnapshot.data
                if (data != null && data.containsKey(dayToRemoveValueFrom)) {
                    val dayArray =
                        data[dayToRemoveValueFrom] as ArrayList<String> // Use ArrayList<String>

                    // Remove the specific value from the array
                    dayArray.remove(valueToRemove)

                    // Create a new data map with the updated array
                    val updatedData = HashMap<String, Any>()
                    updatedData[dayToRemoveValueFrom] = dayArray

                    // Update the Firestore document with the modified data
                    docRef.update(updatedData).addOnSuccessListener {
                        // The value has been removed successfully
                        activity.readScheduleCloud(devId)
                        Toast.makeText(activity,"Successfully Removed",Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener { e ->
                        // Handle any errors that occurred during the update
                        Toast.makeText(activity,"Remove Failed",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

}