package com.example.psusensor

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment

class PhoneOfflineDialogFragment: DialogFragment() {

    var isShowed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        isShowed = true
        super.onCreate(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { activity ->
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(activity)
            val root = activity.layoutInflater.inflate(R.layout.fragment_phone_offline, activity.findViewById(R.id.phone_offline_container))

            builder.setView(root)
            // Create the AlertDialog object and return it
            isCancelable = false
            builder.create().apply {
                setCanceledOnTouchOutside(false)
            }
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        isShowed = false
    }
}