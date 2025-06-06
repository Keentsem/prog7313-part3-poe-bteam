package com.example.pocketsafe.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.example.pocketsafe.R
import com.example.pocketsafe.data.Subscription
import com.example.pocketsafe.data.RenewalPeriod
import java.util.*

/**
 * Custom view to visualize subscription costs as a bar chart
 * Uses the pixel-retro theme styling with gold (#f3c34e) and brown (#5b3f2c) colors
 */
class SubscriptionBarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Gold color #f3c34e for the bars
    private val barPaint = Paint().apply {
        color = Color.parseColor("#f3c34e") // Gold color from pixel-retro theme
        isAntiAlias = false // For pixel-art look
        style = Paint.Style.FILL
    }
    
    // Brown color #5b3f2c for outlines
    private val barOutlinePaint = Paint().apply {
        color = Color.parseColor("#5b3f2c") // Brown color from pixel-retro theme
        isAntiAlias = false // For pixel-art look
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    
    // Brown color text
    private val textPaint = Paint().apply {
        color = Color.parseColor("#5b3f2c") // Brown color
        textSize = 30f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }
    
    private var pixelFont: Typeface? = null
    private val categories = mutableMapOf<String, Float>()
    private var maxValue = 0f
    private var pixelSize = 4
    
    init {
        // Load the pixel_game font for the retro theme
        try {
            pixelFont = ResourcesCompat.getFont(context, R.font.pixel_game)
            textPaint.typeface = pixelFont
        } catch (e: Exception) {
            // Fallback to default font if pixel_game isn't available
        }
    }
    
    fun setSubscriptions(subscriptions: List<Subscription>) {
        try {
            categories.clear()
            
            if (subscriptions.isEmpty()) {
                invalidate()
                return
            }
            
            // Group subscriptions by category and sum their costs
            subscriptions.forEach { subscription ->
                if (subscription.activeStatus) {
                    // Normalize all to monthly cost
                    val monthlyCost = when (subscription.renewalPeriod) {
                        RenewalPeriod.DAILY -> subscription.amount * 30
                        RenewalPeriod.WEEKLY -> subscription.amount * 4
                        RenewalPeriod.MONTHLY -> subscription.amount
                        RenewalPeriod.QUARTERLY -> subscription.amount / 3
                        RenewalPeriod.YEARLY -> subscription.amount / 12
                    }
                    
                    // Get category name from ID or use "Other" if it's 0 or invalid
                    val category = if (subscription.category <= 0) "Other" else subscription.category.toString()
                    categories[category] = (categories[category] ?: 0f) + monthlyCost.toFloat()
                }
            }
            
            // Find the max value for scaling
            maxValue = categories.values.maxByOrNull { it } ?: 0f
            
            invalidate()
        } catch (e: Exception) {
            Log.e("SubscriptionChart", "Error setting subscriptions: ${e.message}", e)
        }
    }
    
    override fun onDraw(canvas: Canvas) {
        try {
            super.onDraw(canvas)
            
            if (categories.isEmpty() || maxValue <= 0 || width <= 0 || height <= 0) {
                return
            }
            
            // Safety check to avoid division by zero
            if (categories.size <= 0) {
                return
            }
            
            val barWidth = width.toFloat() / (categories.size * 2)
            if (barWidth <= 0) {
                return
            }
            
            val maxHeight = height * 0.7f
            
            categories.entries.forEachIndexed { index, entry ->
                val left = index * barWidth * 2 + barWidth / 2
                val barHeight = (entry.value / maxValue) * maxHeight
                val top = height - barHeight - 80 // Leave space for text
                
                // Draw the bar with pixelated style
                val rect = RectF(left, top, left + barWidth, height - 80f)
                
                // Draw in a pixelated style for retro look
                drawPixelatedRect(canvas, rect, barPaint, barOutlinePaint, pixelSize)
                
                // Draw category text
                canvas.drawText(
                    entry.key, 
                    left + barWidth / 2, 
                    height - 40f, 
                    textPaint
                )
                
                // Draw amount text
                val formattedAmount = "$${"%%.2f".format(entry.value)}"
                canvas.drawText(
                    formattedAmount, 
                    left + barWidth / 2, 
                    top - 10, 
                    textPaint
                )
            }
        } catch (e: Exception) {
            Log.e("SubscriptionChart", "Error drawing chart: ${e.message}", e)
        }
    }
    
    private fun drawPixelatedRect(canvas: Canvas, rect: RectF, fillPaint: Paint, outlinePaint: Paint, pixelSize: Int) {
        try {
            // Ensure positive dimensions
            if (rect.width() <= 0 || rect.height() <= 0) {
                return
            }
            
            val left = (rect.left / pixelSize).toInt() * pixelSize
            val top = (rect.top / pixelSize).toInt() * pixelSize
            val right = ((rect.right / pixelSize).toInt() + 1) * pixelSize
            val bottom = ((rect.bottom / pixelSize).toInt() + 1) * pixelSize
            
            // Safety check for valid dimensions
            if (right <= left || bottom <= top) {
                return
            }
            
            // Fill the rectangle with pixel-art style squares
            for (x in left until right step pixelSize) {
                for (y in top until bottom step pixelSize) {
                    canvas.drawRect(
                        x.toFloat(), 
                        y.toFloat(), 
                        (x + pixelSize).toFloat(), 
                        (y + pixelSize).toFloat(), 
                        fillPaint
                    )
                }
            }
            
            // Draw the outline for the pixel-retro look
            canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), outlinePaint)
        } catch (e: Exception) {
            Log.e("SubscriptionChart", "Error drawing pixelated rect: ${e.message}", e)
        }
    }
}
