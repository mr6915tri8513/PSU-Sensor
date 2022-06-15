package com.example.psusensor.drawer.monthly_electricity_bill

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.psusensor.Communicator
import com.example.psusensor.R
import com.example.psusensor.databinding.FragmentMonthlyElectricityBillPageBinding
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.sqrt

class MonthlyElectricityBillPage: Fragment() {

    private lateinit var monthlyElectricityBillViewModel: MonthlyElectricityBillViewModel
    private lateinit var communicator: Communicator
    private var _binding: FragmentMonthlyElectricityBillPageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        communicator = activity as Communicator

        monthlyElectricityBillViewModel = ViewModelProvider(this)[MonthlyElectricityBillViewModel::class.java]

        _binding = FragmentMonthlyElectricityBillPageBinding.inflate(inflater, container, false)
        val view = binding.root

        val yearArray = arrayOf("2022", "2021", "2020")
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item_row, yearArray)
        binding.spinner.adapter = adapter
        binding.spinner.setSelection(monthlyElectricityBillViewModel.spinnerPosition)

        var year = Calendar.getInstance().get(Calendar.YEAR)

        fun setDataOfTheYear() {
            val destiny = resources.displayMetrics.density.toDouble()
            var width = binding.monthlyElectricityBillWeb.width / destiny
            var height = binding.monthlyElectricityBillWeb.height / destiny
            val size = width * height
            Log.e("size", "$width $height $destiny")
            if (size > 0) {
                if (size > 300000.0) {
                    val scale = sqrt(300000.0 / size)
                    width *= scale
                    height *= scale
                }
                communicator.getPowerOfTheYear(year) { data ->
                    val url = "https://chart.googleapis.com/chart?cht=bvs&chds=0,50&" +
                            "chs=${width.toInt()}x${height.toInt()}&" +
                            "chd=t:${data}&chco=33CCFF,99FF99&chxt=x,y&chxl=0:|1-2|3-4|5-6|7-8|9-10|11-12|1:||NT\$10|NT\$20|NT\$30|NT\$40|NT\$50&chbh=a"
                    binding.monthlyElectricityBillWeb.loadUrl(url)
                }
            }
        }

        binding.spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                monthlyElectricityBillViewModel.spinnerPosition = position
                year = yearArray[position].toInt()
                setDataOfTheYear()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        Timer().schedule(500, 200) {
            if (_binding == null) {
                cancel()
            } else {
                if (binding.monthlyElectricityBillWeb.width > 0) {
                    requireActivity().runOnUiThread {
                        if (binding.monthlyElectricityBillWeb.url == null) {
                            setDataOfTheYear()
                        }
                    }
                    cancel()
                }
            }
        }

        communicator.setDrawerChecked(0, 1, true)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        communicator.setDrawerChecked(0, 1, false)
        _binding = null
    }
}