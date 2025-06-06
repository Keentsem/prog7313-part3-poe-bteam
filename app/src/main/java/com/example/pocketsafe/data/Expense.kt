package com.example.pocketsafe.data

import androidx.room.*

/**
 * Represents an expense with category, amount, and date information
 * Compatible with both Room database and Firebase Firestore
 * Styled for pixel-retro theme with gold (#f3c34e) and brown (#5b3f2c) colors
 */
@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoryId")]
)
data class Expense(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "categoryId")
    val categoryId: Int,

    @ColumnInfo(name = "description", defaultValue = "NULL")
    val description: String? = null,

    @ColumnInfo(name = "photoUri", defaultValue = "NULL")
    val photoUri: String? = null,

    @ColumnInfo(name = "startDate")
    val startDate: String,

    @ColumnInfo(name = "endDate")
    val endDate: String,

    @ColumnInfo(name = "date")
    val date: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "firebaseId")
    val firebaseId: String = "", // For Firebase document ID
    
    @ColumnInfo(name = "category", defaultValue = "NULL")
    val category: String? = null, // For category name reference
    
    @ColumnInfo(name = "lastUpdated")
    val lastUpdated: Long = System.currentTimeMillis()
) {
    /**
     * Convert local Room ID to a Firebase document ID string
     * This ensures compatibility between Room and Firebase
     */
    @Ignore
    fun getDocumentId(): String {
        return if (firebaseId.isNotEmpty()) firebaseId else id.toString()
    }
    
    /**
     * Get category type enum value from categoryId
     */
    @Ignore
    fun getCategoryType(): CategoryType {
        return when (categoryId) {
            1 -> CategoryType.FOOD
            2 -> CategoryType.SHOPPING
            3 -> CategoryType.TRANSPORTATION
            4 -> CategoryType.ENTERTAINMENT
            5 -> CategoryType.UTILITIES
            6 -> CategoryType.HEALTHCARE
            7 -> CategoryType.EDUCATION
            8 -> CategoryType.SUBSCRIPTION
            9 -> CategoryType.NECESSITY
            10 -> CategoryType.SPORTS
            11 -> CategoryType.MEDICAL
            else -> CategoryType.OTHER
        }
    }
}