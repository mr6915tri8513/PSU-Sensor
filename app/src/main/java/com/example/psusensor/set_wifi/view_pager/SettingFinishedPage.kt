package com.example.psusensor.set_wifi.view_pager

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.psusensor.R
import com.example.psusensor.databinding.FragmentSettingFinishedPageBinding
import com.example.psusensor.set_wifi.dialog_fragment.QueryDialogFragment
import com.example.psusensor.set_wifi.dialog_fragment.RemindDialogFragment

class SettingFinishedPage(): Fragment() {

    private var _binding: FragmentSettingFinishedPageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingFinishedPageBinding.inflate(inflater, container, false)
        val view = binding.root

        Log.e("view pager", "setting finished")
        val bluetoothManager = requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        binding.backHomeBtn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.BLUETOOTH_ADMIN
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                QueryDialogFragment(R.string.query_disable_bluetooth) { result ->
                    if (result) {
                        bluetoothManager.adapter.disable()
                    }
                    findNavController().navigate(R.id.main_page)
                }.show(parentFragmentManager, "query_disable_bluetooth")
            } else {
                RemindDialogFragment(R.string.remind_disable_bluetooth) {
                    findNavController().navigate(R.id.main_page)
                    bluetoothManager.adapter.disable()//TODO disable
                }.show(parentFragmentManager, "remind_disable_bluetooth")
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}