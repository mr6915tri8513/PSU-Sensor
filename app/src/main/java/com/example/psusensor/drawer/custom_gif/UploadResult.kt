package com.example.psusensor.drawer.custom_gif

import androidx.annotation.IntDef

class UploadResult {
    @IntDef
    @Retention
    annotation class UploadResultInt

    companion object {
        const val DOWNLOADING = 0
        const val CANCELED = 1
        const val FAILED = 2
        const val SUCCESS = 3
    }
}