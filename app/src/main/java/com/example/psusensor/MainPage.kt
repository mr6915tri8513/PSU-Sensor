package com.example.psusensor

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.psusensor.databinding.FragmentMainPageBinding
import com.example.psusensor.set_wifi.dialog_fragment.WarningDialogFragment
import com.google.android.material.snackbar.Snackbar
import java.util.*
import kotlin.concurrent.schedule


class MainPage : Fragment() {

    private lateinit var communicator: Communicator
    private var _binding: FragmentMainPageBinding? = null
    private val binding get() = _binding!!
    private val powerViewModel: PowerViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainPageBinding.inflate(inflater, container, false)
        val view = binding.root

        Log.e("main page", "onCreateView")

        communicator = activity as Communicator
        communicator.setSupportActionBar()

        val activity = activity as AppCompatActivity
        activity.supportActionBar?.show()
        //TODO check if this is home page

        powerViewModel.isDeviceOnline.observe(viewLifecycleOwner) { isOnline ->
            binding.computerStateTv.setText(
                if (isOnline == true) R.string.computer_state_online
                else R.string.computer_state_offline
            )
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}