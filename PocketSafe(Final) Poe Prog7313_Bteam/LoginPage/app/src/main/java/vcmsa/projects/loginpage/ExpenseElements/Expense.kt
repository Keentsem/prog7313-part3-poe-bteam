package vcmsa.projects.loginpage.ExpenseElements

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val firebaseId: String? = null, // Firebase document ID for syncing
    val categoryName: String,
    val amount: Double,
    val description: String,
    val startDate: String,
    val endDate: String,
    val photoUrl: String? = null // URL to the uploaded photo in Firebase Storage
)
