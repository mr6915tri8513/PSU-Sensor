package com.example.psusensor.set_wifi.dialog_fragment

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import com.example.psusensor.R

class WarningDialogFragment(private val message: Int): DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
                builder.setMessage(getString(message))
                .setPositiveButton(R.string.confirm) { dialog, id ->
                    Log.e("dialog", "confirm")
                }
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}