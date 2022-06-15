package com.example.psusensor.set_wifi.dialog_fragment

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class LoadingDialogFragment(private val message: Int): DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
                builder.setMessage(getString(message))
            // Create the AlertDialog object and return it
            isCancelable = false
            builder.create().apply {
                setCanceledOnTouchOutside(false)
            }
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}