package com.toyproject.ecosave.models

data class RelativeElectricPowerConsumeGradeData(
    val deviceType: Int, // 0: 냉장고, 1: 에어컨, 2: TV, 3: 세탁기, 4: 전자레인지, 5: 보일러
    val relativeElectricPowerConsumeGrade: Int,
    val relativeElectricPowerConsumePercentage: Int,
    val powerOfConsume: Float,
    val unit: Int // 0: kWh/월, 1: W
)
