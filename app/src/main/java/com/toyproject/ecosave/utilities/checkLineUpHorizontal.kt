package com.toyproject.ecosave.utilities

import android.graphics.Rect
import android.util.Log

fun checkLineUpHorizontal(rectLeft: Rect?, rectRight: Rect?, rect: Rect?) : Boolean {
    if (rectLeft == null || rectRight == null || rect == null) {
        return false
    }

    val centerY = rect.exactCenterY()

    Log.d("라이브프리뷰", "${rectLeft.top}, $centerY, ${rectLeft.bottom}")
    Log.d("라이브프리뷰", "${rectRight.top}, $centerY, ${rectRight.bottom}")
    Log.d("라이브프리뷰", "${rectLeft.right}, ${rect.left}")
    Log.d("라이브프리뷰", "${rect.right}, ${rectRight.left}")
    Log.d("라이브프리뷰", "===================================")

    return ((rectLeft.top <= centerY)
            && (centerY <= rectLeft.bottom)
            && (rectRight.top <= centerY)
            && (centerY <= rectRight.bottom)
            && (rectLeft.right <= rect.left)
            && (rect.right <= rectRight.left))
}