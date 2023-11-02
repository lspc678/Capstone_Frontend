package com.toyproject.ecosave.models

data class RelativeElectricPowerConsumeGradeData(
    val deviceType: DeviceTypeList,
    val relativeElectricPowerConsumeGrade: Int,
    val relativeElectricPowerConsumePercentage: Int,
    val powerOfConsume: Float,
    val unit: Int // 0: kWh/월, 1: W
)
