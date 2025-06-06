package com.example.pocketsafe

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.room.Room
import androidx.work.Configuration
import com.example.pocketsafe.data.AppDatabase
import com.example.pocketsafe.util.PreferenceHelper
import com.example.pocketsafe.worker.SubscriptionReminderWorker
import com.example.pocketsafe.worker.SubscriptionWorkScheduler
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Main Application class without Hilt dependencies to resolve crash issues
 * Once the app is stable, we can reintroduce Hilt properly
 */
class MainApplication : Application(), Configuration.Provider {
    companion object {
        private const val TAG = "PocketSafeApp"
        
        // Application instance for ViewModelFactory pattern
        @Volatile
        lateinit var instance: MainApplication
            private set
        
        // Singleton instance of AppDatabase to avoid issues with multiple instances
        @Volatile
        private var dbInstance: AppDatabase? = null
        
        // Singleton instance for PreferenceHelper
        @Volatile
        private var preferenceHelperInstance: PreferenceHelper? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return dbInstance ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pocket_safe_database_v11"
                )
                .fallbackToDestructiveMigration() // Force recreation on schema changes
                .build()
                dbInstance = instance
                instance
            }
        }
        
        fun getPreferenceHelper(context: Context): PreferenceHelper {
            return preferenceHelperInstance ?: synchronized(this) {
                preferenceHelperInstance ?: PreferenceHelper.getInstance(context.applicationContext).also { 
                    preferenceHelperInstance = it 
                }
            }
        }
    }
    
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()
        
        // Set instance for ViewModelFactory pattern
        instance = this
        
        // Initialize components in a safe order
        initializeFirebase()
        createNotificationChannel()
        
        // Initialize Room database first before scheduling workers
        initializeRoomDatabase()
        
        // Schedule the subscription reminder work on a background thread to avoid ANR
        GlobalScope.launch(Dispatchers.IO) {
            try {
                SubscriptionWorkScheduler.scheduleReminderWork(applicationContext)
                Log.d(TAG, "Subscription reminders scheduled successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error scheduling subscription reminders: ${e.message}")
            }
        }
    }
    
    private fun initializeFirebase() {
        try {
            FirebaseApp.initializeApp(this)
            
            // Configure Firestore for better offline support
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build()
            
            FirebaseFirestore.getInstance().firestoreSettings = settings
            Log.d(TAG, "Firebase initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firebase: ${e.message}")
            // Don't propagate the exception to avoid app crash
        }
    }
    
    private fun initializeRoomDatabase() {
        try {
            // Initialize the database
            val db = getDatabase(this)
            Log.d(TAG, "Room database initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Room database: ${e.message}")
            // Don't propagate the exception to avoid app crash
        }
    }
    
    private fun createNotificationChannel() {
        try {
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
                Log.d(TAG, "Notification channel created successfully")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating notification channel: ${e.message}")
            // Don't propagate the exception to avoid app crash
        }
    }
    
    // Implement WorkManager configuration to avoid crashes
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()
}
