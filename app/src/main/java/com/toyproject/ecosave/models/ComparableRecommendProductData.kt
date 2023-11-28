package com.toyproject.ecosave.models

data class ComparableRecommendProductData(
    var recommendProductData: RecommendProductData) : Comparable<ComparableRecommendProductData> {
    override fun compareTo(other: ComparableRecommendProductData): Int {
        val res = recommendProductData.powerOfConsume?.minus(other.recommendProductData.powerOfConsume!!)
        return if (res == null) {
            0
        } else if (res > 0.0) {
            1
        } else {
            -1
        }
    }
}
