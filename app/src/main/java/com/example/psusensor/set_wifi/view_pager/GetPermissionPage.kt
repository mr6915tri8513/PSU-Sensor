package com.example.psusensor.set_wifi.view_pager

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.psusensor.R
import com.example.psusensor.databinding.FragmentGetPermissionPageBinding
import com.example.psusensor.set_wifi.SetWiFiViewPagerAdapter
import com.example.psusensor.set_wifi.dialog_fragment.WarningDialogFragment

class GetPermissionPage(private val adapter: SetWiFiViewPagerAdapter): Fragment() {

    private var _binding: FragmentGetPermissionPageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGetPermissionPageBinding.inflate(inflater, container, false)
        val view = binding.root

        Log.e("view pager", "get permission")
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            permissions.forEach { permission ->
                Log.e("get permission", "${permission.key} ${permission.value}")
            }
            if (permissions.values.contains(false)) {
                WarningDialogFragment(R.string.allow_permission_warning)
                    .show(parentFragmentManager, "allow_permission_warning")
            } else {
                adapter.nextPage()
            }
        }

        binding.allowPermissionBtn.setOnClickListener {
            requestPermissionLauncher.launch(adapter.getPermissionStrings())
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}