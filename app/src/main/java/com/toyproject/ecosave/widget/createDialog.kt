package com.toyproject.ecosave.widget

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

fun createDialog(
    context: Context,
    title: String,
    message: String,
    positiveButtonOnClickListener: DialogInterface.OnClickListener,
    negativeButtonOnClickListener: DialogInterface.OnClickListener
) {
    val alertDialogBuilderBtn = AlertDialog.Builder(context)
    alertDialogBuilderBtn.setTitle(title)
    alertDialogBuilderBtn.setMessage(message)

    alertDialogBuilderBtn.setPositiveButton("확인", positiveButtonOnClickListener)
    alertDialogBuilderBtn.setNegativeButton("취소", negativeButtonOnClickListener)

    val alertDialogBox = alertDialogBuilderBtn.create()
    alertDialogBox.show()
}