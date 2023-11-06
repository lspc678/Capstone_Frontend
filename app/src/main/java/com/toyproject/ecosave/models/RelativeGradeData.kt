package com.toyproject.ecosave.models

data class RelativeGradeData(
    val deviceType: DeviceTypeList,
    val relativeElectricPowerConsumeGrade: Int,
    val relativeElectricPowerConsumePercentage: Int,
    val powerOfConsume: Float,
    val relativeCO2EmissionGrade: Int?,
    val relativeCO2EmissionPercentage: Int?,
    val amountOfCO2Emission: Float?,
)
