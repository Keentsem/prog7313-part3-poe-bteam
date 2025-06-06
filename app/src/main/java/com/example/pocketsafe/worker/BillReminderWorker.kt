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
import com.example.pocketsafe.data.BillReminder
import com.example.pocketsafe.MainActivity
// Removed flow.first import as we're using direct method calls instead
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Worker to handle bill payment reminders
 * Uses the pixel-retro theme styling with gold (#f3c34e) and brown (#5b3f2c) colors
 */
class BillReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID = "bill_reminders"
        const val NOTIFICATION_GROUP = "com.example.pocketsafe.BILL_REMINDERS"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d("BillReminderWorker", "Starting bill reminder check")
            
            // Get the database instance using the updated singleton pattern
            val database = com.example.pocketsafe.MainApplication.getDatabase(context)

            // Get upcoming bills
            val bills = try {
                // Use a different method without Flow since we're in a worker
                database.billReminderDao().getUnpaidBills()
            } catch (e: Exception) {
                Log.e("BillReminderWorker", "Error getting unpaid bills: ${e.message}")
                return@withContext Result.failure()
            }
            
            Log.d("BillReminderWorker", "Found ${bills.size} unpaid bills")

            // Current time
            val calendar = Calendar.getInstance()
            val currentTime = calendar.timeInMillis

            // Check for upcoming bills
            var notificationCounter = 0
            
            // Default notification threshold (in days) for all bills
            val DEFAULT_NOTIFICATION_DAYS = 3
            
            (bills as List<BillReminder>).forEach { bill ->
                // Skip if already paid
                if (bill.paid) return@forEach
                
                try {
                    // How many days until the bill is due
                    val daysUntilDue = TimeUnit.MILLISECONDS.toDays(
                        bill.dueDate - currentTime
                    )
                    
                    Log.d("BillReminderWorker", "Bill ${bill.id}: $daysUntilDue days until due")
                    
                    // If it's within the default notification threshold (3 days)
                    if (daysUntilDue in 0..DEFAULT_NOTIFICATION_DAYS.toLong()) {
                        // Show notification for upcoming bill
                        // Use the title hash instead of ID to handle String IDs
                        sendBillNotification(
                            bill.title.hashCode() + notificationCounter,
                            bill.title,
                            bill.amount,
                            daysUntilDue
                        )
                        notificationCounter++
                    }
                } catch (e: Exception) {
                    Log.e("BillReminderWorker", "Error processing bill ${bill.id}: ${e.message}")
                    // Skip this bill and continue with others
                    return@forEach
                }
            }

            Log.d("BillReminderWorker", "Worker completed successfully")
            return@withContext Result.success()
        } catch (e: Exception) {
            Log.e("BillReminderWorker", "Worker failed with exception: ${e.message}", e)
            return@withContext Result.failure()
        }
    }

    private fun sendBillNotification(
        notificationId: Int,
        billDescription: String,
        amount: Double,
        daysRemaining: Long
    ) {
        // Create notification channel for API 26+
        createNotificationChannel()
        
        // Create an intent that opens the main activity
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Add action to navigate to bill reminders tab
            action = "OPEN_BILL_REMINDERS"
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
            .setSmallIcon(R.drawable.bill_clock) // Using pixel-themed bill clock icon
            .setContentTitle("Bill Payment Due")
            .setContentText("$billDescription: $${String.format("%.2f", amount)} due in $dayText")
            .setColor(android.graphics.Color.parseColor("#f3c34e")) // Gold color from pixel-retro theme
            .setStyle(NotificationCompat.BigTextStyle()
                .setBigContentTitle("Bill Due Soon")
                .bigText("Your bill payment of $${String.format("%.2f", amount)} for $billDescription is due in $dayText. Tap to view details.")
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
            val name = "Bill Payment Reminders"
            val descriptionText = "Notifications for upcoming bill payments"
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
