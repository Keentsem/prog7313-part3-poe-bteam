package com.example.pocketsafe.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Handles scheduling of subscription reminder work
 */
object SubscriptionWorkScheduler {
    private const val SUBSCRIPTION_REMINDER_WORK_NAME = "subscription_reminder_work"

    /**
     * Schedules periodic checks for upcoming subscription payments
     * Runs once a day to check for subscriptions due in the next few days
     */
    fun scheduleReminderWork(context: Context) {
        // Create a work request to run once a day
        val reminderWorkRequest = PeriodicWorkRequestBuilder<SubscriptionReminderWorker>(
            1, TimeUnit.DAYS  // Check once per day
        ).build()

        // Schedule the work, replacing any existing scheduled work
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SUBSCRIPTION_REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            reminderWorkRequest
        )
    }

    /**
     * Cancels all subscription reminder work
     */
    fun cancelReminderWork(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(SUBSCRIPTION_REMINDER_WORK_NAME)
    }
}
