package com.example.psusensor.set_wifi

import android.Manifest
import android.bluetooth.BluetoothSocket
import android.os.Build
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.psusensor.set_wifi.view_pager.*
import java.util.*
import kotlin.concurrent.schedule

class SetWiFiViewPagerAdapter(fragment: Fragment, private val viewPager: ViewPager2): FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 5

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> GetPermissionPage(this)
            1 -> EnableBluetoothPage(this)
            2 -> BluetoothConnectionPage(this)
            3 -> DataTransferPage(this)
            4 -> SettingFinishedPage()
            else -> Fragment()
        }
    }

    var socket: BluetoothSocket? = null

    fun nextPage() {
        viewPager.currentItem++
    }

    fun getPermissionStrings(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        } else {
            arrayOf(
                //Manifest.permission.BLUETOOTH_PRIVILEGED,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        }
}