package com.example.psusensor.set_wifi.view_pager

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.CompanionDeviceManager
import android.companion.WifiDeviceFilter
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.psusensor.Communicator
import com.example.psusensor.R
import com.example.psusensor.databinding.FragmentBluetoothConnectionPageBinding
import com.example.psusensor.set_wifi.SetWiFiViewPagerAdapter
import com.google.android.material.snackbar.Snackbar
import java.util.*
import java.util.regex.Pattern
import kotlin.concurrent.schedule

class BluetoothConnectionPage(private val adapter: SetWiFiViewPagerAdapter): Fragment() {

    private lateinit var communicator: Communicator
    private var _binding: FragmentBluetoothConnectionPageBinding? = null
    private val binding get() = _binding!!
    private val deviceManager: CompanionDeviceManager by lazy {
        requireActivity().getSystemService(Context.COMPANION_DEVICE_SERVICE) as CompanionDeviceManager
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBluetoothConnectionPageBinding.inflate(inflater, container, false)
        val view = binding.root

        Log.e("view pager", "bluetooth connection")
        communicator = activity as Communicator

        val uuid = communicator.getUUID()

        fun BluetoothSocket.readLine(timeout: Long?, line: (String?) -> Unit) {
            val buffer = StringBuffer()
            var count = 0L
            val period = 250L
            Timer().schedule(250, period) {
                if (inputStream.available() > 0) {
                    val num = inputStream.available()
                    Log.e("bluetooth", "available $num")
                    for (i in 0 until num) {
                        val peek = inputStream.read()
                        if (peek != 13) {
                            //Log.e("read", peek.toString())
                            buffer.append(peek.toChar())
                        } else {
                            inputStream.read()
                            break
                        }
                    }
                    cancel()
                    Log.e("bluetooth", "receive: $buffer")
                    line(buffer.toString())
                } else if (timeout != null) {
                    count += period
                    if (count >= timeout) {
                        line(null)
                    }
                }
            }
        }

        fun BluetoothSocket.writeLine(line: String) {
            val message = line + '\n'
            outputStream.write(message.toByteArray())
        }

        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.getParcelableExtra<BluetoothDevice>(CompanionDeviceManager.EXTRA_DEVICE)?.let { device ->
                    if (ActivityCompat.checkSelfPermission(
                            requireContext(),
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Manifest.permission.BLUETOOTH_CONNECT
                            else Manifest.permission.BLUETOOTH_ADMIN
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        binding.inProgressTv.setText(R.string.connecting_to_device)
                        Log.e("uuids", device.uuids.map { it.uuid }.toString())
                        adapter.socket = device.createRfcommSocketToServiceRecord(uuid.uuid).apply {
                            connect()
                            writeLine("pair_request")
                            readLine(null) { line ->
                                if (line == "request_allowed") {
                                    requireActivity().runOnUiThread {
                                        adapter.nextPage()
                                    }
                                }
                            }
                        }
                    } else {
                        Snackbar.make(requireView(), R.string.permission_denied, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }

        // To skip filters based on names and supported feature flags (UUIDs),
        // omit calls to setNamePattern() and addServiceUuid()
        // respectively, as shown in the following  Bluetooth example.
        val deviceFilter: BluetoothDeviceFilter = BluetoothDeviceFilter.Builder()
            .setNamePattern(Pattern.compile("PSU Sensor"))
            .addServiceUuid(uuid, null)//TODO crash on Android 8
            .build()

        val filter: WifiDeviceFilter = WifiDeviceFilter.Builder()
            .setNamePattern(Pattern.compile("PSU"))
            .build()

        // The argument provided in setSingleDevice() determines whether a single
        // device name or a list of them appears.
        val pairingRequest: AssociationRequest = AssociationRequest.Builder()
            .addDeviceFilter(deviceFilter)//TODO ?????
            //.addDeviceFilter(filter)
            .setSingleDevice(true)
            .build()

        // When the app tries to pair with a Bluetooth device, show the
        // corresponding dialog box to the user.
        deviceManager.associate(pairingRequest,
            object : CompanionDeviceManager.Callback() {

                override fun onDeviceFound(chooserLauncher: IntentSender) {
                    Log.e("bluetooth", "device found")
                    //startIntentSenderForResult(chooserLauncher, SELECT_DEVICE_REQUEST_CODE, null, 0, 0, 0)
                    val intentSenderRequest = IntentSenderRequest.Builder(chooserLauncher).build()
                    resultLauncher.launch(intentSenderRequest)
                }

                override fun onFailure(error: CharSequence?) {
                    // Handle the failure.
                    Log.e("bluetooth", "device found failed $error")
                }
            }, null)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}