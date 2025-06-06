package com.example.pocketsafe

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.room.Room
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.pocketsafe.data.AppDatabase
import com.example.pocketsafe.worker.SubscriptionReminderWorker
import com.example.pocketsafe.worker.SubscriptionWorkScheduler
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * PocketSafe Application class 
 * Modified to work without Hilt to prevent app crashes
 * Maintains pixel-retro theme styling with gold (#f3c34e) and brown (#5b3f2c) colors
 */
class PocketSafeApplication : Application() {
    // Create application scope for coroutines
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    companion object {
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pocketsafe_database"
                )
                .fallbackToDestructiveMigration() // Allows database recreation if schema changes
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase
        try {
            FirebaseApp.initializeApp(this)
            
            // Configure Firestore for better offline support
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build()
            
            FirebaseFirestore.getInstance().firestoreSettings = settings
            Log.d("PocketSafeApp", "Firebase initialized successfully")
        } catch (e: Exception) {
            Log.e("PocketSafeApp", "Error initializing Firebase: ${e.message}")
        }
        
        // Initialize database with singleton pattern
        try {
            // Initialize the database
            val database = getDatabase(this)
            Log.d("PocketSafeApp", "Database initialized successfully")
        } catch (e: Exception) {
            Log.e("PocketSafeApp", "Error initializing database: ${e.message}")
        }
        
        // Create notification channels for Android O and above
        createNotificationChannel()
        
        // Initialize subscription reminders
        applicationScope.launch {
            try {
                SubscriptionWorkScheduler.scheduleReminderWork(applicationContext)
                Log.d("PocketSafeApp", "Subscription reminders scheduled successfully")
            } catch (e: Exception) {
                Log.e("PocketSafeApp", "Error scheduling subscription reminders: ${e.message}")
            }
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Subscription Reminders"
            val descriptionText = "Notifications for upcoming subscription payments"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(SubscriptionReminderWorker.CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                lightColor = android.graphics.Color.parseColor("#f3c34e") // Gold color for pixel-retro theme
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}