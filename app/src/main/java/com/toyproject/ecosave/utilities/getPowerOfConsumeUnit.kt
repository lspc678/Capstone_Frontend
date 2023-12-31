package com.toyproject.ecosave.utilities

import com.toyproject.ecosave.models.DeviceTypeList

fun getPowerOfConsumeUnit(deviceType: DeviceTypeList?) : Map<String, String> {
    return when (deviceType) {
        DeviceTypeList.REFRIGERATOR -> mapOf(
            "description" to "월간소비전력량",
            "symbol" to "kWh/월"
        )
        DeviceTypeList.AIR_CONDITIONER -> mapOf(
            "description" to "월간소비전력량",
            "symbol" to "kWh/월"
        )
        DeviceTypeList.TV -> mapOf(
            "description" to "소비전력",
            "symbol" to "W"
        )
        DeviceTypeList.WASHING_MACHINE,
        DeviceTypeList.DRYER -> mapOf(
            "description" to "1kg당 소비전력량",
            "symbol" to "Wh/kg"
        )
        DeviceTypeList.MICROWAVE_OVEN -> mapOf(
            "description" to "정격소비전력",
            "symbol" to "W"
        )
        DeviceTypeList.BOILER -> mapOf(
            "description" to "난방열효율",
            "symbol" to "%"
        )
        else -> mapOf(
            "description" to "Error",
            "symbol" to "Error"
        )
    }
}