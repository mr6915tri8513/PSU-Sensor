package com.example.psusensor.set_wifi.view_pager

import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.example.psusensor.Communicator
import com.example.psusensor.R
import com.example.psusensor.databinding.FragmentDataTransferPageBinding
import com.example.psusensor.set_wifi.SetWiFiViewPagerAdapter
import com.example.psusensor.set_wifi.dialog_fragment.LoadingDialogFragment
import com.google.android.material.snackbar.Snackbar
import java.util.*
import kotlin.concurrent.schedule

class DataTransferPage(private val adapter: SetWiFiViewPagerAdapter): Fragment() {

    private lateinit var communicator: Communicator
    private var _binding: FragmentDataTransferPageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDataTransferPageBinding.inflate(inflater, container, false)
        val view = binding.root

        Log.e("view pager", "data transfer")
        communicator = activity as Communicator

        fun BluetoothSocket.readLine(line: (String) -> Unit) {
            val buffer = StringBuffer()
            Timer().schedule(250, 250) {
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
                }
            }
        }

        fun BluetoothSocket.writeLines(lines: List<String>) {
            val message = lines.joinToString("\n") + '\n'
            outputStream.write(message.toByteArray())
        }

        fun showSnackBar(resId: Int) {
            Snackbar.make(requireView(), resId, Snackbar.LENGTH_LONG).show()
        }

        /* TODO why create data transfer view before device found
        if (adapter.socket == null) {
            binding.ssidEt.isEnabled = false
            binding.passwordEt.isEnabled = false
            showSnackBar(R.string.socket_is_null)
        }*/

        binding.ssidEt.addTextChangedListener {
            binding.sendBtn.isEnabled = !it.isNullOrEmpty()
        }

        binding.sendBtn.setOnClickListener {
            if (adapter.socket == null) {
                showSnackBar(R.string.socket_is_null)
            }
            adapter.socket?.apply {
                val dialog = LoadingDialogFragment(R.string.waiting_for_device)
                dialog.show(parentFragmentManager, "waiting_for_device")
                val ssid = binding.ssidEt.text.toString()
                val password = binding.passwordEt.text.toString()
                Log.e("bluetooth", "ssid: $ssid password: $password")
                writeLines(listOf("send_data", ssid, password))
                readLine { line1 ->
                    Log.e("bluetooth", "receive: $line1")
                    if (line1 == "data_received") {
                        readLine { line2 ->
                            Log.e("bluetooth", "receive: $line2")
                            when (line2) {
                                "connection_success" -> {
                                    Log.e("bluetooth", "connection_success")
                                    adapter.socket?.close()
                                    adapter.socket = null
                                    dialog.dismiss()
                                    requireActivity().runOnUiThread {
                                        adapter.nextPage()
                                    }
                                }
                                "connection_failed" -> {
                                    Log.e("bluetooth", "connection_failed")
                                    showSnackBar(R.string.wrong_wifi_data)
                                    dialog.dismiss()
                                }
                                else -> {
                                    Log.e("bluetooth", "connection_?????")
                                    showSnackBar(R.string.invalid_response)
                                    dialog.dismiss()
                                }
                            }
                        }
                    }

                }
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}