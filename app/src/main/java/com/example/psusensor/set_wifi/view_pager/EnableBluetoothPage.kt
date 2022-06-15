package com.example.psusensor.set_wifi.view_pager

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.psusensor.databinding.FragmentEnableBluetoothPageBinding
import com.example.psusensor.set_wifi.SetWiFiViewPagerAdapter

class EnableBluetoothPage(private val adapter: SetWiFiViewPagerAdapter): Fragment() {

    private var _binding: FragmentEnableBluetoothPageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEnableBluetoothPageBinding.inflate(inflater, container, false)
        val view = binding.root

        Log.e("view pager", "enable bluetooth")
        val bluetoothManager = requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        if (bluetoothManager.adapter.isEnabled) {
            adapter.nextPage()
        }

        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Log.e("bluetooth", "enabled")
                adapter.nextPage()
            }
        }

        binding.enableBluetoothBtn.setOnClickListener {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            resultLauncher.launch(intent)
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}