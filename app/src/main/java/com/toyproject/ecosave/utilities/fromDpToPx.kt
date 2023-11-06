package com.toyproject.ecosave.utilities

import android.content.res.Resources
import android.util.DisplayMetrics

fun fromDpToPx(resources: Resources, dp: Float): Int {
    val metrics = resources.displayMetrics
    val px = dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    return px.toInt()
}