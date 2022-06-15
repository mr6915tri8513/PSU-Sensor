package com.example.psusensor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.*

class PowerViewModel: ViewModel() {

    private var data: Queue<Double> = LinkedList(List(30) { 0.0 })
    private var lastUpdateTime: LocalDateTime = LocalDateTime.now()
    private var deviceOfflineCount = 0
    private val _power = MutableLiveData<Pair<Double, Double>?>(null)
    val power: LiveData<Pair<Double, Double>?> = _power
    private val _isDeviceOnline = MutableLiveData(false)
    val isDeviceOnline: LiveData<Boolean> = _isDeviceOnline
    private val _powerLimit = MutableLiveData<Int?>(null)
    val powerLimit: LiveData<Int?> = _powerLimit

    fun isNeedToUpdate(dateTime: LocalDateTime): Boolean {
        return lastUpdateTime < dateTime
    }

    fun updateData(powerList: List<Pair<Double, Double>?>, dateTime: LocalDateTime): Boolean {
        lastUpdateTime = dateTime
        data = LinkedList(powerList.map { power ->
            power?.let {
                it.first * it.second
            }?: 0.0
        })
        _power.value = powerList.last()
        deviceOfflineCount = 0
        if (_isDeviceOnline.value != true) {
            _isDeviceOnline.value = true
        }
        powerList.last()?.let {
            return isOverload(it.first * it.second)
        }
        return false
    }

    fun nextSecond() {
        data.remove()
        data.add(0.0)
        _power.value = null
        if (deviceOfflineCount < 3) {
            deviceOfflineCount++
        } else if (_isDeviceOnline.value != false) {
            _isDeviceOnline.value = false
        }
    }

    fun getDataString(): String {
        return powerLimit.value?.let { limit ->
            val limitArray = Array(30) { limit }
            "${data.joinToString(",")}|${limitArray.joinToString(",")}"
        }?: data.joinToString(",")
    }

    fun updatePowerLimit(limit: Int) {
        _powerLimit.value = limit
    }

    private fun isOverload(power: Double): Boolean {
        _powerLimit.value?.let {
            return power > it
        }
        return false
    }
}