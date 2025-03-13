package com.mfa.view.custom

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.mfa.R

class CustomAlertDialog(private val context: Context) {

    fun showDialog(title: String, message: String, onYesClick: () -> Unit) {
        val dialog = Dialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.activity_custom_alert_dialog, null)

        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val titleText = view.findViewById<TextView>(R.id.dialogTitle)
        val messageText = view.findViewById<TextView>(R.id.dialogMessage)
        val btnYes = view.findViewById<Button>(R.id.btnYes)
        val btnNo = view.findViewById<Button>(R.id.btnNo)

        titleText.text = title
        messageText.text = message

        btnYes.setOnClickListener {
            onYesClick()
            dialog.dismiss()
        }

        btnNo.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}