package com.example.psusensor.drawer.instant_power_curve

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Selection
import android.util.Log
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import com.example.psusensor.R

class SetPowerLimitDialogFragment(
    private val currentPowerLimit: Int?,
    private val newPowerLimit: (Int?) -> Unit
) : DialogFragment() {

    private var _newPowerLimit: Int? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { activity ->
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(activity)
            val root = activity.layoutInflater.inflate(R.layout.fragment_set_power_limit, activity.findViewById(R.id.set_power_limit_container))

            val powerLimitEt: EditText = root.findViewById(R.id.power_limit_et)

            if (currentPowerLimit != null) {
                powerLimitEt.setText(getString(R.string.watt_int, currentPowerLimit))
            }

            powerLimitEt.addTextChangedListener {
                Log.e("text_watcher", it.toString())
                if (!it.isNullOrEmpty()) {
                    val limit = it.toString()
                    if (limit == "W") {
                        powerLimitEt.setText("")
                    } else if (!limit.endsWith('W')) {
                        powerLimitEt.setText(getString(R.string.watt_string, limit.substringBefore('W')))
                        powerLimitEt.setSelection(powerLimitEt.text.length - 1)
                    } else if (limit.trimEnd('W').toInt() > 300) {
                        powerLimitEt.setText(getString(R.string.watt_string, "300"))
                        powerLimitEt.setSelection(3)
                    }
                }
            }

            builder.setView(root)
                .setPositiveButton(R.string.confirm) { dialog, which ->
                Log.e("set_power_limit", powerLimitEt.text.toString())
                _newPowerLimit = powerLimitEt.text.toString().trimEnd('W').toIntOrNull()
            }
                .setNegativeButton(R.string.cancel) { dialog, which ->
                _newPowerLimit = null
            }
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onDismiss(dialog: DialogInterface) {
        newPowerLimit(_newPowerLimit)
        super.onDismiss(dialog)
    }
}