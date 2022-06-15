package com.example.psusensor.set_wifi.dialog_fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import com.example.psusensor.R

class RemindDialogFragment(private val message: Int, private val result: () -> Unit): DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setMessage(getString(message))
                .setPositiveButton(R.string.ok) { dialog, id ->
                    Log.e("dialog", "ok")
                    result()
                }
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        Log.e("dialog", "cancel")
        result()
    }
}