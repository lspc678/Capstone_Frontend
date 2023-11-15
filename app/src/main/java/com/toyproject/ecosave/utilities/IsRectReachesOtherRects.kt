package com.toyproject.ecosave.utilities

import android.graphics.Rect

fun isRectReachesOtherRects (
    rectForDescription: Rect?, rect: Rect?, rectForUnit: Rect?) : Boolean {
    // rect 객체가 rectForDescription, rectForUnit 객체를 포함하는지 확인
    if (rectForDescription == null || rect == null || rectForUnit == null) {
        return false
    }

    // rect 객체가 rectForDescription, rectForUnit 객체를 포함한다고 판단할 수 있는 최대 오차(px 단위)
    val ALLOW_ERROR_PX = 0

    val rectForDescriptionLeft = rectForDescription.left
    val rectForUnitRight = rectForUnit.right

    return ((rectForDescriptionLeft - ALLOW_ERROR_PX <= rect.left)
            && (rect.right <= rectForUnitRight + ALLOW_ERROR_PX))
}