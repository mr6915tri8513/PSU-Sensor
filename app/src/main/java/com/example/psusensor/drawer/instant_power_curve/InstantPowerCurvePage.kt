package com.example.psusensor.drawer.instant_power_curve

import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import com.example.psusensor.Communicator
import com.example.psusensor.PowerViewModel
import com.example.psusensor.R
import com.example.psusensor.databinding.FragmentInstantPowerCurvePageBinding
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.sqrt

class InstantPowerCurvePage: Fragment() {

    private lateinit var communicator: Communicator
    private val powerViewModel: PowerViewModel by activityViewModels()
    private var _binding: FragmentInstantPowerCurvePageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        communicator = activity as Communicator

        //val galleryViewModel = ViewModelProvider(this)[InstantPowerCurveViewModel::class.java]

        _binding = FragmentInstantPowerCurvePageBinding.inflate(inflater, container, false)
        val view = binding.root

        var webWidth = 0
        var webHeight = 0

        fun drawPowerPerSecond() {
            val destiny = resources.displayMetrics.density.toDouble()
            if (webWidth == 0) {
                webWidth = binding.instantPowerCurveWeb.width
            }
            if (webHeight == 0) {
                webHeight = binding.instantPowerCurveWeb.height
            }
            var width = webWidth / destiny
            var height = webHeight / destiny
            val size = width * height
            //Log.d("size", "$width $height $destiny")
            if (size > 300000.0) {
                val scale = sqrt(300000.0 / size)
                width *= scale
                height *= scale
            }
            if (size != 0.0) {
                val url = "https://chart.googleapis.com/chart?cht=ls&chds=0,300&" +
                        "chs=${(width).toInt()}x${(height).toInt()}&" +
                        "chd=t:${powerViewModel.getDataString()}&chco=33CCFF,FF0000&chxt=x,y&chxl=0:|30s ago|Now|1:||100W|200W|300W"
                //Log.d("url", url)
                binding.instantPowerCurveWeb.loadUrl(url)
            }
        }

        powerViewModel.power.observe(viewLifecycleOwner) {
            drawPowerPerSecond()
        }

        var powerLimit: Int? = null

        powerViewModel.powerLimit.observe(viewLifecycleOwner) { limit ->
            powerLimit = limit
            if (limit == null) {
                binding.currentPowerLimitTv.setText(R.string.current_power_limit_default)
            } else {
                binding.currentPowerLimitTv.text = getString(R.string.current_power_limit, limit)
            }
        }

        binding.setPowerLimitBtn.setOnClickListener {
            SetPowerLimitDialogFragment(powerLimit) { limit ->
                Log.e("set_power_limit", limit.toString())
                limit?.let { communicator.setPowerLimit(it) }
            }.show(parentFragmentManager, "set_power_limit")
        }

        val date = Calendar.getInstance()

        fun setDataOfTheDay() {
            val destiny = resources.displayMetrics.density.toDouble()
            var width = binding.dailyPowerUsageWeb.width / destiny
            var height = binding.dailyPowerUsageWeb.height / destiny
            val size = width * height
            Log.e("size", "$width $height $destiny")
            if (size > 300000.0) {
                val scale = sqrt(300000.0 / size)
                width *= scale
                height *= scale
            }
            binding.dateTv.text = getString(R.string.date, date.get(Calendar.YEAR), date.get(Calendar.MONTH) + 1, date.get(Calendar.DAY_OF_MONTH))
            if (size != 0.0) {
                communicator.getPowerOfTheDay(date.get(Calendar.YEAR), date.get(Calendar.MONTH) + 1, date.get(Calendar.DAY_OF_MONTH)) { data ->
                    val url = "https://chart.googleapis.com/chart?cht=bvs&chds=0,300&" +
                            "chs=${width.toInt()}x${height.toInt()}&" +
                            "chd=t:${data.joinToString(","){ (it?:0.0).toString() }}&chco=33CCFF&chxt=x,y&chxl=0:|0AM||||||6AM||||||12PM||||||18PM||||||1:||100Wh|200Wh|300Wh&chbh=a"
                    Log.e("url", url)
                    binding.dailyPowerUsageWeb.loadUrl(url)
                }
            }
        }

        binding.selectDateBtn.setOnClickListener {
            DatePickerDialogFragment(date) {
                setDataOfTheDay()
            }.show(parentFragmentManager, "date_picker")
        }

        Timer().schedule(500, 200) {
            if (_binding == null) {
                cancel()
            } else {
                if (binding.dailyPowerUsageWeb.width > 0) {
                    requireActivity().runOnUiThread {
                        if (binding.dailyPowerUsageWeb.url == null) {
                            setDataOfTheDay()
                        }
                    }
                    cancel()
                }
            }
        }

        communicator.setDrawerChecked(0, 0, true)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        communicator.setDrawerChecked(0, 0, false)
        _binding = null
    }
}