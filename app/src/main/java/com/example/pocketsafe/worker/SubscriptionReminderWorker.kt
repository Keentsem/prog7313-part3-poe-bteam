package com.example.pocketsafe.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.pocketsafe.R
import com.example.pocketsafe.data.AppDatabase
import com.example.pocketsafe.ui.activity.SubscriptionListActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Worker to handle subscription payment reminders
 * Uses the pixel-retro theme styling with gold (#f3c34e) and brown (#5b3f2c) colors
 */
class SubscriptionReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID = "subscription_reminders"
        const val NOTIFICATION_GROUP = "com.example.pocketsafe.SUBSCRIPTION_REMINDERS"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d("SubscriptionWorker", "Starting subscription reminder check")
            
            // Get the database instance using the updated singleton pattern
            val database = com.example.pocketsafe.MainApplication.getDatabase(context)

            // Get active subscriptions
            val subscriptions = try {
                database.subscriptionDao().getActiveSubscriptionsList()
            } catch (e: Exception) {
                Log.e("SubscriptionWorker", "Error getting active subscriptions: ${e.message}")
                return@withContext Result.failure()
            }
            
            Log.d("SubscriptionWorker", "Found ${subscriptions.size} active subscriptions")

            // Current time
            val calendar = Calendar.getInstance()
            val currentTime = calendar.timeInMillis

            // Check for upcoming subscriptions
            var notificationCounter = 0
            
            // Default notification threshold (in days) for all subscriptions
            val DEFAULT_NOTIFICATION_DAYS = 3
            
            for (subscription in subscriptions) {
                // Skip processing if subscription is not active
                if (!subscription.activeStatus) continue
                
                try {
                    // How many days until renewal
                    val daysUntilRenewal = TimeUnit.MILLISECONDS.toDays(
                        subscription.nextDueDate - currentTime
                    )
                    
                    Log.d("SubscriptionWorker", "Subscription ${subscription.name}: $daysUntilRenewal days until renewal")
                    
                    // If it's within the default notification threshold (3 days)
                    if (daysUntilRenewal in 0..DEFAULT_NOTIFICATION_DAYS.toLong()) {
                        // Show notification for upcoming subscription
                        // Use a notification ID based on a combination of subscription name hash and counter
                        // This works with both numeric and string IDs
                        sendSubscriptionNotification(
                            (subscription.name.hashCode() + notificationCounter),
                            subscription.name,
                            subscription.amount,
                            daysUntilRenewal
                        )
                        notificationCounter++
                    }
                } catch (e: Exception) {
                    Log.e("SubscriptionWorker", "Error processing subscription ${subscription.id}: ${e.message}")
                    continue
                }
            }

            Log.d("SubscriptionWorker", "Worker completed successfully")
            return@withContext Result.success()
        } catch (e: Exception) {
            Log.e("SubscriptionWorker", "Worker failed with exception: ${e.message}", e)
            return@withContext Result.failure()
        }
    }

    private fun sendSubscriptionNotification(
        notificationId: Int,
        subscriptionName: String,
        amount: Double,
        daysRemaining: Long
    ) {
        // Create notification channel for API 26+
        createNotificationChannel()
        
        // Create an intent that opens the subscription list
        val intent = Intent(context, SubscriptionListActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Create the notification with pixel-retro styling
        val dayText = if (daysRemaining == 0L) "TODAY!" else "$daysRemaining days"
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.sub_due_alert) // Using pixel-themed subscription alert icon
            .setContentTitle("$subscriptionName Payment Due")
            .setContentText("$${String.format("%.2f", amount)} due in $dayText")
            .setColor(android.graphics.Color.parseColor("#f3c34e")) // Gold color from pixel-retro theme
            .setStyle(NotificationCompat.BigTextStyle()
                .setBigContentTitle("$subscriptionName Due Soon")
                .bigText("Your subscription payment of $${String.format("%.2f", amount)} is due in $dayText. Tap to view details.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setGroup(NOTIFICATION_GROUP)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
        
        // Show the notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Subscription Reminders"
            val descriptionText = "Notifications for upcoming subscription payments"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                // Use pixel-retro theme gold color
                enableLights(true)
                lightColor = 0xf3c34e // Gold color from pixel-retro theme
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
