package com.toyproject.ecosave.utilities

import android.graphics.Rect
import android.util.Log

fun checkLineUpHorizontal(rectLeft: Rect?, rectCenter: Rect?, rectRight: Rect?) : Boolean {
    if (rectLeft == null || rectRight == null || rectCenter == null) {
        return false
    }

    // 3개의 Rect 객체가 같은 가로줄에 있다고 판단할 수 있는 최대 오차(px 단위)
    val ALLOW_ERROR_PX = 0

    val rectLeftCenterY = rectLeft.exactCenterY()
    val rectCenterY = rectCenter.exactCenterY()
    val rectRightCenterY = rectRight.exactCenterY()

    val rectLeftHeight = rectLeft.height()
    val rectCenterHeight = rectCenter.height()
    val rectRightHeight = rectRight.height()

    val maxHeight = maxOf(rectLeftHeight, rectCenterHeight, rectRightHeight)

    Log.d("라이브프리뷰", "${rectLeft.top}, ${rectLeft.bottom}")
    Log.d("라이브프리뷰", "${rectCenter.top}, ${rectCenter.bottom}")
    Log.d("라이브프리뷰", "${rectRight.top}, ${rectRight.bottom}")
    Log.d("라이브프리뷰", "===================================")

    return when (maxHeight) {
        rectLeftHeight -> {
            ((rectLeft.top - ALLOW_ERROR_PX <= rectCenterY)
                    && (rectCenterY <= rectLeft.bottom + ALLOW_ERROR_PX)
                    && (rectLeft.top - ALLOW_ERROR_PX <= rectRightCenterY)
                    && (rectRightCenterY <= rectLeft.bottom + ALLOW_ERROR_PX))
        }
        rectCenterHeight -> {
            ((rectCenter.top - ALLOW_ERROR_PX <= rectLeftCenterY)
                    && (rectLeftCenterY <= rectCenter.bottom + ALLOW_ERROR_PX)
                    && (rectCenter.top - ALLOW_ERROR_PX <= rectRightCenterY)
                    && (rectRightCenterY <= rectCenter.bottom + ALLOW_ERROR_PX))
        }
        rectRightHeight -> {
            ((rectRight.top - ALLOW_ERROR_PX <= rectLeftCenterY)
                    && (rectLeftCenterY <= rectRight.bottom + ALLOW_ERROR_PX)
                    && (rectRight.top - ALLOW_ERROR_PX <= rectCenterY)
                    && (rectCenterY <= rectRight.bottom + ALLOW_ERROR_PX))
        }
        else -> {
            return false
        }
    }
}

fun checkLineUpHorizontal(rectLeft: Rect?, rectRight: Rect?) : Boolean {
    if (rectLeft == null || rectRight == null) {
        return false
    }

    val rectLeftCenterY = rectLeft.exactCenterY()
    val rectRightCenterY = rectRight.exactCenterY()

    Log.d("라이브프리뷰", "${rectRight.top}, $rectLeftCenterY, ${rectRight.bottom}")
    Log.d("라이브프리뷰", "${rectLeft.top}, $rectRightCenterY, ${rectLeft.bottom}")
    Log.d("라이브프리뷰", "===================================")

    return ((rectRight.top <= rectLeftCenterY)
            && (rectLeftCenterY <= rectRight.bottom)
            && (rectLeft.top <= rectRightCenterY)
            && (rectRightCenterY <= rectLeft.bottom))
}