package com.example.fishfeeder.view

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.fishfeeder.R
import com.example.fishfeeder.control.DeviceViewModel
import com.example.fishfeeder.control.DeviceViewModelFactory
import com.example.fishfeeder.model.Device
import com.example.fishfeeder.model.DeviceApplication
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.*

class DeviceAdapter(
    private val activity: MainActivity,
) :
    RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {
    var devices: List<Device> = ArrayList()
    val db = FirebaseFirestore.getInstance()
    lateinit var deviceViewModel: DeviceViewModel

    class DeviceViewHolder(devView: View) : RecyclerView.ViewHolder(devView) {
        val containerClose: LinearLayout = devView.findViewById(R.id.closeCardBox)
        val smallTextTitle: TextView = devView.findViewById(R.id.txtSmallTitleCard)
        val containerOpen: LinearLayout = devView.findViewById(R.id.openContainerCard)
        val textTitle: TextView = devView.findViewById(R.id.txtTitleCard)
        val txtValueFoodVolume: TextView = devView.findViewById(R.id.txtValueFoodVolume)
        val txtValueLastFeed: TextView = devView.findViewById(R.id.txtValLastFeedTime)
        val percentBarFoodVolume: ProgressBar = devView.findViewById(R.id.percentBarFoodVolume)
        val btnEditDev: Button = devView.findViewById(R.id.btnEdit)
        val txtValLastUpdate: TextView = devView.findViewById(R.id.txtValLastUpdate)
        val containerTopOpen: LinearLayout = devView.findViewById(R.id.openContainerTop)
        val containerBottomOpen: LinearLayout = devView.findViewById(R.id.openContainerBottom)
        val drawableContainerTopOpen: GradientDrawable =
            containerTopOpen.background as GradientDrawable
        val drawableContainerBottomOpen: GradientDrawable =
            containerBottomOpen.background as GradientDrawable
        val drawableContainerClose: GradientDrawable = containerClose.background as GradientDrawable
        var listenerRegistration: ListenerRegistration? = null
        val warnText: TextView = devView.findViewById(R.id.txtWarningMsg)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_device_card, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {

        fun setColor(color: Int) {
            holder.drawableContainerTopOpen.setColor(color)
            holder.drawableContainerBottomOpen.setColor(color)
            holder.drawableContainerClose.setColor(color)
            holder.btnEditDev.setTextColor(color)
        }

        fun updateWarning(foodVol: Double?, foodMin: Double?) {
            val redWarning = ContextCompat.getColor(activity, R.color.base_red)
            val yellowWarning = ContextCompat.getColor(activity, R.color.base_yellow)
            val greenSafe = ContextCompat.getColor(activity, R.color.base_green)

            if (foodVol != null) {
                if (foodVol <= 0.0) {
                    setColor(redWarning)
                    holder.warnText.setText("Food Feeder empty! Please refill")
                } else if (foodVol > 0 && foodVol < foodMin!!) {
                    setColor(yellowWarning)
                    holder.warnText.setText("Food volume is less than minimal value")
                } else {
                    setColor(greenSafe)
                    holder.warnText.setText("Food volume is within safe limits")
                }
            }
        }

        // Initialize deviceViewModel
        val viewModelFactory =
            DeviceViewModelFactory((activity.application as DeviceApplication).repository)
        deviceViewModel =
            ViewModelProvider(activity, viewModelFactory).get(DeviceViewModel::class.java)

        val currentDev: Device = devices[position]

        updateWarning(currentDev.afterFeedVol, currentDev.minFoodVol)

        val listenerRegistration = db.collection("Devices")
            .document(currentDev.devID)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    // Handle errors here
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    // Check for changes in the specified fields
                    val newAfterFeed = snapshot.getDouble("afterFeedVol")
                    val newBeforeFeed = snapshot.getDouble("beforeFeedVol")
                    val newMinFoodVol = snapshot.getDouble("minFoodVol")
                    val lastFeedTime: String = snapshot.getString("lastFeedTime").toString()
                    if (lastFeedTime != "0" && lastFeedTime != "") {
                        val month = lastFeedTime.substring(0, 2).toInt()
                        val date = lastFeedTime.substring(2, 4).toInt()
                        val hour = lastFeedTime.substring(4, 6).toInt()
                        val minute = lastFeedTime.substring(6, 8).toInt()
                        val formattedHour =
                            String.format("%02d", hour) // Format hour with leading zero if needed
                        val formattedMinute = String.format(
                            "%02d",
                            minute
                        ) // Format minute with leading zero if needed
                        val feedMonth = when (month) {
                            1 -> "Jan"
                            2 -> "Feb"
                            3 -> "Mar"
                            4 -> "Apr"
                            5 -> "May"
                            6 -> "Jun"
                            7 -> "Jul"
                            8 -> "Aug"
                            9 -> "Sep"
                            10 -> "Oct"
                            11 -> "Nov"
                            12 -> "Des"
                            else -> "Invalid day number"
                        }
                        holder.txtValueLastFeed.text =
                            "$feedMonth $date, $formattedHour:$formattedMinute"
                    } else {
                        holder.txtValueLastFeed.text = "Haven't done any feeding"
                    }


                    val calendar = Calendar.getInstance()
                    val dateFormat = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
                    val formattedDateTime = dateFormat.format(calendar.time)

                    // Display the result in the TextView
                    holder.txtValLastUpdate.text = "$formattedDateTime"

                    if (newAfterFeed != null && newBeforeFeed != null &&
                        (newAfterFeed != currentDev.afterFeedVol || newBeforeFeed != currentDev.beforeFeedVol || newMinFoodVol != currentDev.minFoodVol)
                    ) {
//                        Toast.makeText(activity, "there is a change", Toast.LENGTH_SHORT).show()
//                        val calendar = Calendar.getInstance()
//                        val dateFormat = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
//                        val formattedDateTime = dateFormat.format(calendar.time)
//                        // Display the result in the TextView
//                        holder.txtValLastUpdate.text = "$formattedDateTime"
                        if (newMinFoodVol != null) {
                            updateWarning(newAfterFeed, newMinFoodVol)
                        }
                        val updatedDevice = Device(
                            devID = currentDev.devID,
                            titleDev = currentDev.titleDev,
                            beforeFeedVol = newBeforeFeed,
                            afterFeedVol = newAfterFeed,
                            lastFeedTimeStamp = currentDev.lastFeedTimeStamp,
                            allowNotif = currentDev.allowNotif,
                            isOwner = currentDev.isOwner,
                            minFoodVol = newMinFoodVol
                        )
                        updatedDevice.devNum = currentDev.devNum
                        deviceViewModel.update(updatedDevice)
//                        Toast.makeText(activity, "Updated", Toast.LENGTH_SHORT).show()
                        // Update cached values
                        currentDev.afterFeedVol = newAfterFeed
                        currentDev.beforeFeedVol = newBeforeFeed

                        holder.txtValueFoodVolume.text = newAfterFeed.toString()
                        holder.percentBarFoodVolume.progress = newAfterFeed.toInt()

                        // Notify the adapter that the data has changed
                        notifyDataSetChanged()
                    }
                }
            }

        holder.listenerRegistration = listenerRegistration

        holder.containerClose.setOnClickListener() {
            holder.containerClose.visibility = View.GONE
            holder.containerOpen.visibility = View.VISIBLE
        }

        holder.containerTopOpen.setOnClickListener() {
            holder.containerClose.visibility = View.VISIBLE
            holder.containerOpen.visibility = View.GONE
        }
        holder.btnEditDev.setOnClickListener() {
            val intent = Intent(activity, EditActivity::class.java)
            val devNum = currentDev.devNum // Replace this with your actual data
            intent.putExtra(
                "DevNum",
                devNum.toString()
            )
            intent.putExtra("device", currentDev)
            activity.startActivity(intent)
        }
        holder.txtValueFoodVolume.text = String.format("%.0f%%", currentDev.afterFeedVol)
        holder.percentBarFoodVolume.setProgress(currentDev.afterFeedVol?.toInt() ?: 0)
        holder.textTitle.text = currentDev.titleDev
        holder.smallTextTitle.text = currentDev.titleDev
    }

    override fun getItemCount(): Int {
        return devices.size
    }

    fun setDevice(myDevices: List<Device>) {
        this.devices = myDevices
        notifyDataSetChanged()
    }
}