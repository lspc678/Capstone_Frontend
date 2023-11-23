package com.toyproject.ecosave.models

data class RegisteredDeviceData(
    // 기기 종류
    val deviceType: DeviceTypeList,
    
    // 전력 소비 상대 등급
    val relativeElectricPowerConsumeGrade: Int,

    // 전력 소비 누적 비율(%)
    val relativeElectricPowerConsumePercentage: Int,
    
    // 전력 소비량
    val powerOfConsume: Float,

    // CO2 배출량이 적혀있지 않은 제품의 경우 CO2 배출량 상대 등급과 누적 비율(%)은 동일하다고 가정

    // CO2 배출량 상대 등급
    val relativeCO2EmissionGrade: Int?,

    // CO2 배출량 누적 비율(%)
    val relativeCO2EmissionPercentage: Int?,

    // CO2 배출량
    val amountOfCO2Emission: Float?,

    // 하루 평균 사용 시간
    var averageUsageTimePerDay: Float?
)
