package com.example.pocketsafe

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PocketSafeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Hilt will handle database initialization through DatabaseModule
    }
}