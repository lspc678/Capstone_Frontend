package com.toyproject.ecosave.models

data class RegisteredDeviceData(
    // pkey
    val id: Int,

    // 기기 종류
    val deviceType: DeviceTypeList,

    // 모델명
    var model: String?,
    
    // 전력 소비 상대 등급
    var relativeElectricPowerConsumeGrade: Int?,

    // 전력 소비 누적 비율(%)
    var relativeElectricPowerConsumePercentage: Int?,
    
    // 전력 소비량
    var powerOfConsume: Double?,

    // CO2 배출량이 적혀있지 않은 제품의 경우 CO2 배출량 상대 등급과 누적 비율(%)은 동일하다고 가정

    // CO2 배출량 상대 등급
    var relativeCO2EmissionGrade: Int?,

    // CO2 배출량 누적 비율(%)
    var relativeCO2EmissionPercentage: Int?,

    // CO2 배출량
    var amountOfCO2Emission: Double?,

    // 하루 평균 사용 시간
    var averageUsageTimePerDay: Double?
)
