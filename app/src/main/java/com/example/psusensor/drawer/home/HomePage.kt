package com.example.psusensor.drawer.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import com.example.psusensor.PowerViewModel
import com.example.psusensor.R
import com.example.psusensor.databinding.FragmentHomePageBinding
import com.google.android.material.navigation.NavigationView

class HomePage : Fragment() {

    private val powerViewModel: PowerViewModel by activityViewModels()
    private var _binding: FragmentHomePageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomePageBinding.inflate(inflater, container, false)
        val view = binding.root

        val navigationView: NavigationView = requireActivity().findViewById(R.id.navigation_view)

        binding.instantPowerCurveContainer.setOnClickListener {
            navigationView.menu[0].subMenu[0].onNavDestinationSelected(findNavController())
        }

        binding.monthlyElectricityBillContainer.setOnClickListener {
            navigationView.menu[0].subMenu[1].onNavDestinationSelected(findNavController())
        }

        binding.themeSelectionContainer.setOnClickListener {
            navigationView.menu[1].subMenu[0].onNavDestinationSelected(findNavController())
        }

        binding.customGifContainer.setOnClickListener {
            navigationView.menu[1].subMenu[1].onNavDestinationSelected(findNavController())
        }

        binding.resetWifiConnectionContainer.setOnClickListener {
            navigationView.menu[2].subMenu[0].onNavDestinationSelected(findNavController())
        }

        binding.troubleshootingContainer.setOnClickListener {
            navigationView.menu[2].subMenu[1].onNavDestinationSelected(findNavController())
        }

        binding.languagesContainer.setOnClickListener {
            navigationView.menu[2].subMenu[2].onNavDestinationSelected(findNavController())
        }

        powerViewModel.power.observe(viewLifecycleOwner) { power ->
            when {
                power != null -> {
                    binding.voltageTv.text = getString(R.string.voltage, power.first)
                    binding.currentTv.text = getString(R.string.current, power.second)
                }
                powerViewModel.isDeviceOnline.value == true -> {
                    binding.voltageTv.text = getString(R.string.voltage, 0.0)
                    binding.currentTv.text = getString(R.string.current, 0.0)
                }
                else -> {
                    binding.voltageTv.setText(R.string.voltage_default)
                    binding.currentTv.setText(R.string.current_default)
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