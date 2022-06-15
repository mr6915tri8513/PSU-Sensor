package com.example.psusensor.set_wifi.dialog_fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import com.example.psusensor.R

class QueryDialogFragment(private val message: Int, private val result: (Boolean) -> Unit): DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setMessage(getString(message))
                .setPositiveButton(R.string.yes) { dialog, id ->
                    Log.e("dialog", "yes")
                    result(true)
                }
                .setNegativeButton(R.string.no) { dialog, id ->
                    Log.e("dialog", "no")
                    result(false)
                }
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        Log.e("dialog", "cancel")
        result(false)
    }
}