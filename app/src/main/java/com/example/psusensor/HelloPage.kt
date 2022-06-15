package com.example.psusensor

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.psusensor.databinding.FragmentHelloPageBinding

class HelloPage : Fragment() {

    private var _binding: FragmentHelloPageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHelloPageBinding.inflate(inflater, container, false)
        val view = binding.root

        if (isOnBoardingFinished()) {
            findNavController().navigate(R.id.main_page)
        } else {
            binding.startNowBtn.setOnClickListener {
                onBoardingFinished()
                findNavController().navigate(R.id.set_wifi_page)
            }
        }

        return view
    }

    private fun isOnBoardingFinished(): Boolean {
        val sharedPref = requireActivity().getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("finished", false)
    }

    private fun onBoardingFinished() {
        val sharedPref = requireActivity().getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        if (!sharedPref.getBoolean("finished", false)) {
            val editor = sharedPref.edit()
            editor.putBoolean("finished", true)
            editor.apply()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}