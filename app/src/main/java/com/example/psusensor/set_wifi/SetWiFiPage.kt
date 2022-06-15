package com.example.psusensor.set_wifi

import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.psusensor.Communicator
import com.example.psusensor.R
import com.example.psusensor.databinding.FragmentSetWifiPageBinding
import com.example.psusensor.drawer.theme_selection.ConfirmDialogFragment

class SetWiFiPage: Fragment() {

    private lateinit var communicator: Communicator
    private var _binding: FragmentSetWifiPageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSetWifiPageBinding.inflate(inflater, container, false)
        val view = binding.root

        Log.e("bluetooth", "api level: ${Build.VERSION.SDK_INT}")
        communicator = activity as Communicator

        val adapter = SetWiFiViewPagerAdapter(this, binding.viewPager)
        binding.viewPager.isUserInputEnabled = false
        binding.viewPager.adapter = adapter

        fun checkPermissions(permissions: Array<String>): Boolean {
            return permissions.any { permission ->
                ActivityCompat.checkSelfPermission(
                    requireContext(),
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            }
        }

        if (!checkPermissions(adapter.getPermissionStrings())) {
            val bluetoothManager = requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            if (bluetoothManager.adapter.isEnabled) {
                if (adapter.socket?.isConnected == true) {
                    binding.viewPager.setCurrentItem(3, false)
                } else {
                    binding.viewPager.setCurrentItem(2, false)
                }
            } else {
                binding.viewPager.setCurrentItem(1, false)
            }
        }

        binding.skipBtn.setOnClickListener {
            ConfirmDialogFragment(R.string.skip_wifi_settings) { isPositive ->
                if (isPositive) {
                    if (adapter.socket != null) {
                        if (adapter.socket?.isConnected == true) {
                            adapter.socket?.close()
                        }
                        adapter.socket = null
                    }
                    findNavController().navigate(R.id.main_page)
                }
            }.show(parentFragmentManager, "skip_wifi_settings")
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}