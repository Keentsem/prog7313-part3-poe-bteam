package com.example.pocketsafe.data

/**
 * Enum representing different subscription renewal periods
 */
enum class RenewalPeriod {
    DAILY,
    WEEKLY,
    MONTHLY, 
    QUARTERLY,
    YEARLY;

    companion object {
        fun fromString(value: String): RenewalPeriod {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                MONTHLY // Default to monthly if not recognized
            }
        }

        fun toDisplayString(period: RenewalPeriod): String {
            return when(period) {
                DAILY -> "Daily"
                WEEKLY -> "Weekly"
                MONTHLY -> "Monthly"
                QUARTERLY -> "Quarterly"
                YEARLY -> "Yearly"
            }
        }
    }
}
