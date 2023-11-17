package com.example.fishfeeder

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.fishfeeder.model.Device

class DeviceAdapter(
    private val activity: MainActivity,
) :
    RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {
    var devices: List<Device> = ArrayList()

    class DeviceViewHolder(devView: View) : RecyclerView.ViewHolder(devView) {
        val containerClose: LinearLayout = devView.findViewById(R.id.closeCardBox)
        val smallTextTitle: TextView = devView.findViewById(R.id.txtSmallTitleCard)
        val conteinerOpen: LinearLayout = devView.findViewById(R.id.openContainerCard)
        val closeCardBox: LinearLayout = devView.findViewById(R.id.closeCardBox)
        val textTitle: TextView = devView.findViewById(R.id.txtTitleCard)
        val txtValueFoodVolume: TextView = devView.findViewById(R.id.txtValueFoodVolume)
        val percentBarFoodVolume: ProgressBar = devView.findViewById(R.id.percentBarFoodVolume)
        val btnEditDev: Button = devView.findViewById(R.id.btnEdit)
        val warningBox: LinearLayout = devView.findViewById(R.id.warningContainer)
        val drawable: GradientDrawable = warningBox.background as GradientDrawable
        val drawableSmall: GradientDrawable = containerClose.background as GradientDrawable
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_device_card, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val currentDev: Device = devices[position]
        holder.closeCardBox.setOnClickListener() {
            holder.closeCardBox.visibility = View.GONE
            holder.conteinerOpen.visibility = View.VISIBLE
            //Need to read firestore
        }

        holder.conteinerOpen.setOnClickListener() {
            holder.closeCardBox.visibility = View.VISIBLE
            holder.conteinerOpen.visibility = View.GONE
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
        holder.txtValueFoodVolume.text = currentDev.afterFeedVol.toString()
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