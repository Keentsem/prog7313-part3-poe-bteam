package vcmsa.projects.loginpage.CategoryElements

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,  // autoGenerate: Room auto-increments this value
    val name: String,  // name of the category, e.g., "Food"
    val total: Double  // total amount spent in this category
)
