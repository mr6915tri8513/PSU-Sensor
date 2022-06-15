package com.example.psusensor.drawer

import android.app.Activity
import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.getSystemService
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.psusensor.Communicator
import com.example.psusensor.R
import com.example.psusensor.databinding.FragmentResetWifiConnectionPageBinding
import java.util.*
import java.util.regex.Pattern

class ResetWiFiConnectionPage: Fragment() {

    private lateinit var communicator: Communicator
    private var _binding: FragmentResetWifiConnectionPageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResetWifiConnectionPageBinding.inflate(inflater, container, false)
        val view = binding.root

        communicator = activity as Communicator

        binding.resetWifiBtn.setOnClickListener {
            requireActivity().findNavController(R.id.fragment_container_view_main).navigate(R.id.set_wifi_page)
        }

        communicator.setDrawerChecked(2, 0, true)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        communicator.setDrawerChecked(2, 0, false)
        _binding = null
    }
}