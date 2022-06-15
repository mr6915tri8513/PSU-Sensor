package com.example.psusensor

import android.net.Uri
import android.os.ParcelUuid
import com.example.psusensor.theme.Theme

interface Communicator {
    fun setSupportActionBar()
    fun setDrawerChecked(categoryIndex: Int, itemIndex: Int, isChecked: Boolean)

    fun setTheme(theme: Theme)

    fun compressAndUploadGif(uri: Uri)
    fun uploadGif(uri: Uri, result: (Int) -> Unit)

    fun setPowerLimit(limit: Int)
    fun getPowerOfTheDay(year: Int, month: Int, day: Int, ret: (Array<Double?>) -> Unit)
    fun getPowerOfTheYear(year: Int, ret: (String) -> Unit)

    fun getUUID(): ParcelUuid
}