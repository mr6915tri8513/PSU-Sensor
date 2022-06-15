package com.example.psusensor.drawer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.psusensor.Communicator
import com.example.psusensor.databinding.FragmentLanguagesPageBinding

class LanguagesPage: Fragment() {

    private lateinit var communicator: Communicator
    private var _binding: FragmentLanguagesPageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLanguagesPageBinding.inflate(inflater, container, false)
        val view = binding.root

        communicator = activity as Communicator

        communicator.setDrawerChecked(2, 2, true)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        communicator.setDrawerChecked(2, 2, false)
        _binding = null
    }
}