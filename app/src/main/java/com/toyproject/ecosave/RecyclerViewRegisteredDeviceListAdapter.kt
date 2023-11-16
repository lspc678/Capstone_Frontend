package com.toyproject.ecosave

import android.annotation.SuppressLint
import android.content.Context
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

import com.toyproject.ecosave.models.RelativeGradeData
import com.toyproject.ecosave.utilities.getPowerOfConsumeUnit

class RecyclerViewRegisteredDeviceListAdapter constructor(
    private val context: Context, private val list: List<RelativeGradeData>
) : RecyclerView.Adapter<RecyclerViewRegisteredDeviceListAdapter.ViewHolder>()  {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageDevice: ImageView = itemView.findViewById(R.id.imageDevice)
        val textRelativeElectricPowerConsumeGrade: TextView = itemView.findViewById(R.id.textRelativeElectricPowerConsumeGrade)
        val textPowerOfConsumeType: TextView = itemView.findViewById(R.id.textPowerOfConsumeType)
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
            DeviceTypeList.WASHING_MACHINE -> holder.imageDevice.setImageResource(R.drawable.img_washing_machine)
            DeviceTypeList.MICROWAVE_OVEN -> holder.imageDevice.setImageResource(R.drawable.img_microwave_oven)
            DeviceTypeList.BOILER -> holder.imageDevice.setImageResource(R.drawable.img_boiler)
            else -> holder.imageDevice.setImageResource(R.drawable.ic_image)
        }

        holder.textPowerOfConsumeType.text = getPowerOfConsumeUnit(list[position].deviceType)["description"]
        holder.textPowerOfConsume.text = "${list[position].powerOfConsume} ${getPowerOfConsumeUnit(list[position].deviceType)["symbol"]}"

        when (list[position].relativeElectricPowerConsumeGrade) {
            1 -> holder.textRelativeElectricPowerConsumeGrade.setTextColor(ContextCompat.getColor(context, R.color.grade_1))
            2, 3 -> holder.textRelativeElectricPowerConsumeGrade.setTextColor(ContextCompat.getColor(context, R.color.grade_2_and_3))
            4, 5 -> holder.textRelativeElectricPowerConsumeGrade.setTextColor(ContextCompat.getColor(context, R.color.grade_4_and_5))
            6, 7 -> holder.textRelativeElectricPowerConsumeGrade.setTextColor(ContextCompat.getColor(context, R.color.grade_6_and_7))
            8, 9 -> holder.textRelativeElectricPowerConsumeGrade.setTextColor(ContextCompat.getColor(context, R.color.grade_8_and_9))
            else -> holder.textRelativeElectricPowerConsumeGrade.setTextColor(ContextCompat.getColor(context, R.color.black))
        }

        holder.textRelativeElectricPowerConsumeGrade.text =
            list[position].relativeElectricPowerConsumeGrade.toString() +
                    "등급(" +
                    list[position].relativeElectricPowerConsumePercentage + "%)"

        holder.registeredDeviceListItem.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetailActivity::class.java)

            when (list[position].deviceType) {
                DeviceTypeList.REFRIGERATOR -> intent.putExtra("deviceType", DeviceTypeList.REFRIGERATOR)
                DeviceTypeList.AIR_CONDITIONER -> intent.putExtra("deviceType", DeviceTypeList.AIR_CONDITIONER)
                DeviceTypeList.TV -> intent.putExtra("deviceType", DeviceTypeList.TV)
                DeviceTypeList.WASHING_MACHINE -> intent.putExtra("deviceType", DeviceTypeList.WASHING_MACHINE)
                DeviceTypeList.MICROWAVE_OVEN -> intent.putExtra("deviceType", DeviceTypeList.MICROWAVE_OVEN)
                DeviceTypeList.BOILER -> intent.putExtra("deviceType", DeviceTypeList.BOILER)
                else -> {}
            }

            intent.putExtra("powerOfConsume", list[position].powerOfConsume)
            intent.putExtra("relativeElectricPowerConsumeGrade", list[position].relativeElectricPowerConsumeGrade)
            intent.putExtra("relativeElectricPowerConsumePercentage", list[position].relativeElectricPowerConsumePercentage)
            intent.putExtra("relativeCO2EmissionGrade", list[position].relativeCO2EmissionGrade)
            intent.putExtra("relativeCO2EmissionPercentage", list[position].relativeCO2EmissionPercentage)
            intent.putExtra("amountOfCO2Emission", list[position].amountOfCO2Emission)

            ContextCompat.startActivity(holder.itemView.context, intent, null)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}