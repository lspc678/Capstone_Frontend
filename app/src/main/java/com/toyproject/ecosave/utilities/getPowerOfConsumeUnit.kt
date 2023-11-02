package com.toyproject.ecosave.utilities

import com.toyproject.ecosave.models.DeviceTypeList

fun getPowerOfConsumeUnit(deviceType: DeviceTypeList?) : Map<String, String> {
    return when (deviceType) {
        DeviceTypeList.REFRIGERATOR -> mapOf(
            "description" to "월간소비전력량",
            "symbol" to "kWh/월"
        )
        DeviceTypeList.WASHING_MACHINE -> mapOf(
            "description" to "1kg당 월간소비전력량",
            "symbol" to "Wh/kg"
        )
        DeviceTypeList.AIR_CONDITIONER -> mapOf(
            "description" to "월간소비전력량",
            "symbol" to "kWh/월"
        )
        DeviceTypeList.TV -> mapOf(
            "description" to "소비전력",
            "symbol" to "W"
        )
        DeviceTypeList.BOILER -> mapOf(
            "description" to "정격소비전력",
            "symbol" to "W"
        )
        else -> mapOf(
            "description" to "Error",
            "symbol" to "Error"
        )
    }
}