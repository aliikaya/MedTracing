package com.madtracking.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MedTrackApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Future: Initialize notification channels, work managers, etc.
    }
}

