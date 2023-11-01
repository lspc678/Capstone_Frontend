package com.toyproject.ecosave.widget

import android.content.Context
import androidx.appcompat.app.AlertDialog

fun simpleDialog(context: Context, title: String, message: String) {
    val alertDialogBuilderBtn = AlertDialog.Builder(context)
    alertDialogBuilderBtn.setTitle(title)
    alertDialogBuilderBtn.setMessage(message)
    alertDialogBuilderBtn.setPositiveButton("확인") { _, _ -> }

    val alertDialogBox = alertDialogBuilderBtn.create()
    alertDialogBox.show()
}