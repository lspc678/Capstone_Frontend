package com.toyproject.ecosave.widget

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

// 확인, 취소 버튼이 있는 dialog
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

// 확인 버튼만 존재하는 dialog
fun createDialog(
    context: Context,
    title: String,
    message: String,
    positiveButtonOnClickListener: DialogInterface.OnClickListener,
) {
    val alertDialogBuilderBtn = AlertDialog.Builder(context)
    alertDialogBuilderBtn.setTitle(title)
    alertDialogBuilderBtn.setMessage(message)

    alertDialogBuilderBtn.setPositiveButton("확인", positiveButtonOnClickListener)

    val alertDialogBox = alertDialogBuilderBtn.create()
    alertDialogBox.show()
}