package com.example.pocketsafe.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Helper class to manage app preferences with pixel-retro theme related settings
 * Using singleton pattern since Hilt is temporarily disabled
 */
class PreferenceHelper private constructor(context: Context) {
    
    private val sharedPreferences: SharedPreferences = context
        .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREF_NAME = "PocketSafePrefs"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_SYNC_ENABLED = "sync_enabled"
        private const val KEY_THEME_MODE = "theme_mode" // For potential future theme settings
        
        @Volatile
        private var INSTANCE: PreferenceHelper? = null
        
        fun getInstance(context: Context): PreferenceHelper {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PreferenceHelper(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    fun isFirstLaunch(): Boolean {
        return sharedPreferences.getBoolean(KEY_FIRST_LAUNCH, true)
    }
    
    fun setFirstLaunchComplete() {
        sharedPreferences.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
    }
    
    fun areNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_NOTIFICATION_ENABLED, true)
    }
    
    // Alias for consistency with getter/setter naming pattern
    fun getNotificationsEnabled(): Boolean {
        return areNotificationsEnabled()
    }
    
    fun setNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_NOTIFICATION_ENABLED, enabled).apply()
    }
    
    // Dark mode preference
    fun getDarkMode(): Boolean {
        return sharedPreferences.getBoolean(KEY_DARK_MODE, false)
    }
    
    fun setDarkMode(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }
    
    // Sync preference
    fun getSyncEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_SYNC_ENABLED, true)
    }
    
    fun setSyncEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_SYNC_ENABLED, enabled).apply()
    }
    
    // For potential future use - theme mode toggle
    fun getThemeMode(): String {
        return sharedPreferences.getString(KEY_THEME_MODE, "pixel_retro") ?: "pixel_retro"
    }
    
    fun setThemeMode(mode: String) {
        sharedPreferences.edit().putString(KEY_THEME_MODE, mode).apply()
    }
}
