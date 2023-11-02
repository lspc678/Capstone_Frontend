package com.toyproject.ecosave.utilities

import com.toyproject.ecosave.models.DeviceTypeList

fun getTranslatedDeviceType(deviceType: DeviceTypeList?) : String {
    return when (deviceType) {
        DeviceTypeList.REFRIGERATOR -> "냉장고"
        DeviceTypeList.WASHING_MACHINE -> "세탁기"
        DeviceTypeList.AIR_CONDITIONER -> "에어컨"
        DeviceTypeList.TV -> "TV"
        DeviceTypeList.BOILER -> "보일러"
        else -> ""
    }
}