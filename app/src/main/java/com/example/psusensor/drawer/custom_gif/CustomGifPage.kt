package com.example.psusensor.drawer.custom_gif

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.psusensor.Communicator
import com.example.psusensor.PowerViewModel
import com.example.psusensor.R
import com.example.psusensor.databinding.FragmentCustomGifPageBinding
import com.google.android.material.snackbar.Snackbar

class CustomGifPage: Fragment() {

    private lateinit var communicator: Communicator
    private val powerViewModel: PowerViewModel by activityViewModels()
    private var _binding: FragmentCustomGifPageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomGifPageBinding.inflate(inflater, container, false)
        val view = binding.root

        communicator = activity as Communicator
        var uri: Uri? = null

        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                Log.e("data", data.toString())
                data?.data?.let { it ->
                    Glide.with(this)
                        .load(it)
                        .error(R.drawable.ic_baseline_image_not_supported_48)
                        .into(binding.previewGifImg)
                    binding.selectGifTv.visibility = View.GONE
                    uri = it
                    requireActivity().contentResolver.openInputStream(it)?.let { inputStream ->
                        val size = inputStream.available()
                        inputStream.close()
                        binding.uploadBtn.apply {
                            if (size <= 1048576) {
                                isEnabled = true
                                text = getString(R.string.upload_with_kb, size / 1024)
                            } else {
                                isEnabled = false
                                text = getString(R.string.upload_with_mb, size.toDouble() / 1048576.0)
                                Snackbar.make(
                                    requireView(),
                                    getString(R.string.size_too_big, size.toDouble() / 1048576.0),
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
            }
        }

        binding.previewGifImg.setOnClickListener {
            val intent = Intent()
            intent.type = "image/gif"
            intent.action = Intent.ACTION_GET_CONTENT
            //startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1)
            resultLauncher.launch(Intent.createChooser(intent, getString(R.string.select_gif)))
        }

        binding.uploadBtn.setOnClickListener {
            if (powerViewModel.isDeviceOnline.value != true) {
                Snackbar.make(
                    requireView(),
                    R.string.device_offline,
                    Snackbar.LENGTH_LONG
                ).show()
            } else {
                uri?.let {
                    communicator.uploadGif(it) { result ->
                        Snackbar.make(
                            requireView(),
                            when (result) {
                                UploadResult.DOWNLOADING -> R.string.device_downloading
                                UploadResult.CANCELED -> R.string.upload_canceled
                                UploadResult.FAILED -> R.string.upload_failed
                                UploadResult.SUCCESS -> R.string.upload_success
                                else -> R.string.upload_unknown
                            },
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        communicator.setDrawerChecked(1, 1, true)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        communicator.setDrawerChecked(1, 1, false)
        _binding = null
    }
}