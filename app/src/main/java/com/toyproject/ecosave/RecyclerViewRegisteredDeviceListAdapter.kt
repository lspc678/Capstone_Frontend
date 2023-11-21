package com.toyproject.ecosave

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

import com.google.android.material.switchmaterial.SwitchMaterial
import com.toyproject.ecosave.models.DeviceTypeList
import com.toyproject.ecosave.models.RegisteredDeviceData
import com.toyproject.ecosave.utilities.getPowerOfConsumeUnit
import com.toyproject.ecosave.widget.createDialog

class RecyclerViewRegisteredDeviceListAdapter constructor(
    private val context: Context, private val list: List<RegisteredDeviceData>
) : RecyclerView.Adapter<RecyclerViewRegisteredDeviceListAdapter.ViewHolder>()  {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageDevice: ImageView = itemView.findViewById(R.id.imageDevice)
        val textRelativeElectricPowerConsumeGrade: TextView = itemView.findViewById(R.id.textRelativeElectricPowerConsumeGrade)
        val textPowerOfConsumeType: TextView = itemView.findViewById(R.id.textPowerOfConsumeType)
        val textPowerOfConsume: TextView = itemView.findViewById(R.id.textPowerOfConsume)
        val registeredDeviceListItem: CardView = itemView.findViewById(R.id.registeredDeviceListItem)
        val switchForUseOrNot: SwitchMaterial = itemView.findViewById(R.id.switchForUseOrNot)
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
            // 각각의 등록된 기기에 대한 아이콘 표시
            DeviceTypeList.REFRIGERATOR -> holder.imageDevice.setImageResource(R.drawable.img_refrigerator)
            DeviceTypeList.AIR_CONDITIONER -> holder.imageDevice.setImageResource(R.drawable.img_air_conditioner)
            DeviceTypeList.TV -> holder.imageDevice.setImageResource(R.drawable.img_tv)
            DeviceTypeList.WASHING_MACHINE -> holder.imageDevice.setImageResource(R.drawable.img_washing_machine)
            DeviceTypeList.MICROWAVE_OVEN -> holder.imageDevice.setImageResource(R.drawable.img_microwave_oven)
            DeviceTypeList.BOILER -> holder.imageDevice.setImageResource(R.drawable.img_boiler)
            else -> holder.imageDevice.setImageResource(R.drawable.ic_image)
        }

        // 전력 소비 상대 등급 표시
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

        // 소비전력량 표시
        holder.textPowerOfConsumeType.text = getPowerOfConsumeUnit(list[position].deviceType)["description"]
        holder.textPowerOfConsume.text = "${list[position].powerOfConsume} ${getPowerOfConsumeUnit(list[position].deviceType)["symbol"]}"

        // 각 기기에 대한 사용 여부는 기본적으로 ON으로 설정
        holder.switchForUseOrNot.isChecked = true

        // 사용 여부를 재설정할 때 동작
        // 스위치 ON/OFF에 따라 상대적 에너지 소비 효율 등급 재설정
        // 에어컨, TV, 보일러만 사용 여부를 설정할 수 있음
        when (list[position].deviceType) {
            DeviceTypeList.AIR_CONDITIONER,
            DeviceTypeList.TV,
            DeviceTypeList.BOILER -> {
                holder.switchForUseOrNot.isEnabled = true
                holder.switchForUseOrNot.setOnClickListener {
                    if (holder.switchForUseOrNot.isChecked) {
                        // 사용 여부를 ON으로 설정할 때

                        // 확인 버튼 누를 시
                        val positiveButtonOnClickListener = DialogInterface.OnClickListener { _, _ ->
                            // HomeActivity에 있는 resetPyramid() 함수를 실행함
                            (context as HomeActivity).resetPyramid("ON", list[position])
                        }

                        // 취소 버튼 누를 시
                        val negativeButtonOnClickListener = DialogInterface.OnClickListener { _, _ ->
                            // 다시 OFF로 되돌림
                            holder.switchForUseOrNot.isChecked = false
                        }

                        createDialog(
                            context,
                            "기기 사용 여부 설정",
                            "해당 기기를 사용하는 것으로 설정 하시겠습니까?",
                            positiveButtonOnClickListener,
                            negativeButtonOnClickListener
                        )
                    } else {
                        // 사용 여부를 OFF로 설정할 때

                        // 확인 버튼 누를 시
                        val positiveButtonOnClickListener = DialogInterface.OnClickListener { _, _ ->
                            // HomeActivity에 있는 resetPyramid() 함수를 실행함
                            (context as HomeActivity).resetPyramid("OFF", list[position])
                        }

                        // 취소 버튼 누를 시
                        val negativeButtonOnClickListener = DialogInterface.OnClickListener { _, _ ->
                            // 다시 ON으로 되돌림
                            holder.switchForUseOrNot.isChecked = true
                        }

                        createDialog(
                            context,
                            "기기 사용 여부 설정",
                            "해당 기기를 사용하지 않는 것으로 설정 하시겠습니까?",
                            positiveButtonOnClickListener,
                            negativeButtonOnClickListener
                        )
                    }
                }
            }

            // 나머지 기기의 경우 사용 여부를 설정할 수 없음
            else -> {
                holder.switchForUseOrNot.isEnabled = false
                holder.switchForUseOrNot.isActivated = false
            }
        }

        // 각각의 등록된 기기를 클릭할 때 실행
        holder.registeredDeviceListItem.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetailActivity::class.java)

            // 기기 종류를 intent에 저장
            when (list[position].deviceType) {
                DeviceTypeList.REFRIGERATOR -> intent.putExtra("deviceType", DeviceTypeList.REFRIGERATOR)
                DeviceTypeList.AIR_CONDITIONER -> intent.putExtra("deviceType", DeviceTypeList.AIR_CONDITIONER)
                DeviceTypeList.TV -> intent.putExtra("deviceType", DeviceTypeList.TV)
                DeviceTypeList.WASHING_MACHINE -> intent.putExtra("deviceType", DeviceTypeList.WASHING_MACHINE)
                DeviceTypeList.MICROWAVE_OVEN -> intent.putExtra("deviceType", DeviceTypeList.MICROWAVE_OVEN)
                DeviceTypeList.BOILER -> intent.putExtra("deviceType", DeviceTypeList.BOILER)
                else -> {}
            }
            
            // 소비전력을 intent에 저장
            intent.putExtra("powerOfConsume", list[position].powerOfConsume)

            // 전력 소비 상대 등급을 intent에 저장
            intent.putExtra("relativeElectricPowerConsumeGrade", list[position].relativeElectricPowerConsumeGrade)

            // 전력 소비 누적 비율(%)을 intent에 저장
            intent.putExtra("relativeElectricPowerConsumePercentage", list[position].relativeElectricPowerConsumePercentage)

            // CO2 배출량을 intent에 저장
            intent.putExtra("amountOfCO2Emission", list[position].amountOfCO2Emission)

            // CO2 배출량 상대 등급을 intent에 저장
            intent.putExtra("relativeCO2EmissionGrade", list[position].relativeCO2EmissionGrade)

            // CO2 배출량 누적 비율(%)을 intent에 저장
            intent.putExtra("relativeCO2EmissionPercentage", list[position].relativeCO2EmissionPercentage)

            ContextCompat.startActivity(holder.itemView.context, intent, null)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}