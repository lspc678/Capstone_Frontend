package com.toyproject.ecosave

import android.annotation.SuppressLint

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import com.toyproject.ecosave.models.RelativeElectricPowerConsumeGradeData

class RecyclerViewRegisteredDeviceListAdapter constructor(
    private val getActivity: HomeActivity,
    private val list: List<RelativeElectricPowerConsumeGradeData>)
    : RecyclerView.Adapter<RecyclerViewRegisteredDeviceListAdapter.ViewHolder>()  {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageDevice: ImageView = itemView.findViewById(R.id.imageDevice)
        val textRelativeElectricPowerConsumeGrade: TextView = itemView.findViewById(R.id.textRelativeElectricPowerConsumeGrade)
        val textPowerOfConsume: TextView = itemView.findViewById(R.id.textPowerOfConsume)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.registered_device_list_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        when (list[position].deviceType) {
            0 -> holder.imageDevice.setImageResource(R.drawable.img_refrigerator)
            1 -> holder.imageDevice.setImageResource(R.drawable.img_air_conditioner)
            2 -> holder.imageDevice.setImageResource(R.drawable.ic_tv)
            5 -> holder.imageDevice.setImageResource(R.drawable.img_boiler)
            else -> holder.imageDevice.setImageResource(R.drawable.ic_image)
        }

        holder.textRelativeElectricPowerConsumeGrade.text =
            list[position].relativeElectricPowerConsumeGrade.toString() +
                    "등급(" +
                    list[position].relativeElectricPowerConsumePercentage + "%)"

        when (list[position].unit) {
            0 -> holder.textPowerOfConsume.text = list[position].powerOfConsume.toString() + " kWh/월"
            1 -> holder.textPowerOfConsume.text = list[position].powerOfConsume.toString() + "W"
            else -> holder.textPowerOfConsume.text = "Error"
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}