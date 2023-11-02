package com.toyproject.ecosave

import android.annotation.SuppressLint
import android.content.Intent

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.toyproject.ecosave.models.DeviceTypeList

import com.toyproject.ecosave.models.RelativeElectricPowerConsumeGradeData

class RecyclerViewRegisteredDeviceListAdapter constructor(
    private val list: List<RelativeElectricPowerConsumeGradeData>
) : RecyclerView.Adapter<RecyclerViewRegisteredDeviceListAdapter.ViewHolder>()  {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageDevice: ImageView = itemView.findViewById(R.id.imageDevice)
        val textRelativeElectricPowerConsumeGrade: TextView = itemView.findViewById(R.id.textRelativeElectricPowerConsumeGrade)
        val textPowerOfConsume: TextView = itemView.findViewById(R.id.textPowerOfConsume)
        val registeredDeviceListItem: CardView = itemView.findViewById(R.id.registeredDeviceListItem)
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
            DeviceTypeList.REFRIGERATOR -> holder.imageDevice.setImageResource(R.drawable.img_refrigerator)
            DeviceTypeList.AIR_CONDITIONER -> holder.imageDevice.setImageResource(R.drawable.img_air_conditioner)
            DeviceTypeList.TV -> holder.imageDevice.setImageResource(R.drawable.ic_tv)
            DeviceTypeList.BOILER -> holder.imageDevice.setImageResource(R.drawable.img_boiler)
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

        holder.registeredDeviceListItem.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetailActivity::class.java)

            when (list[position].deviceType) {
                DeviceTypeList.REFRIGERATOR -> intent.putExtra("deviceType", "냉장고")
                DeviceTypeList.AIR_CONDITIONER -> intent.putExtra("deviceType", "에어컨")
                DeviceTypeList.TV -> intent.putExtra("deviceType", "TV")
                DeviceTypeList.BOILER -> intent.putExtra("deviceType", "보일러")
                else -> {}
            }

            ContextCompat.startActivity(holder.itemView.context, intent, null)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}