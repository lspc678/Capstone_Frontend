package com.toyproject.ecosave.utilities

import com.toyproject.ecosave.models.DeviceTypeList

fun getCO2EmissionUnit(deviceType: DeviceTypeList?) : String {
    return when (deviceType) {
        DeviceTypeList.REFRIGERATOR -> "g/시간"
        DeviceTypeList.AIR_CONDITIONER -> "g/시간"
        DeviceTypeList.TV -> "g/시간"
        DeviceTypeList.WASHING_MACHINE,
        DeviceTypeList.DRYER -> "g/회"
        else -> ""
    }
}