// app/src/main/java/com/example/pocketsafe/HiltApplication.kt
package com.example.pocketsafe  // ← Must match your namespace

import android.app.Application
import android.util.Log
import com.example.pocketsafe.worker.SubscriptionWorkScheduler
import dagger.hilt.android.HiltAndroidApp  // ← Import will work after gradle sync

@HiltAndroidApp  // ← This enables Hilt
class PocketSafeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        try {
            // Initialize subscription reminders
            Log.d("PocketSafeApp", "Scheduling subscription reminders")
            SubscriptionWorkScheduler.scheduleReminderWork(this)
        } catch (e: Exception) {
            Log.e("PocketSafeApp", "Error initializing app: ${e.message}")
        }
    }
}