package com.toyproject.ecosave.utilities

import android.content.res.Resources
import android.util.DisplayMetrics

fun fromPxToDp(resources: Resources, px: Float): Int {
    val metrics = resources.displayMetrics
    val dp = px / ((metrics.densityDpi.toFloat()) / DisplayMetrics.DENSITY_DEFAULT)
    return dp.toInt()
}