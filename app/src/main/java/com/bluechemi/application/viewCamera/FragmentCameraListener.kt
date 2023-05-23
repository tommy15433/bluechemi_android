package com.bluechemi.application.viewCamera

import android.net.Uri

interface FragmentCameraListener {
    fun onCapture(uri: Uri)
}