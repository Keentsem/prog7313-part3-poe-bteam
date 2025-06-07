package vcmsa.projects.loginpage.CategoryElements

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CategoryDao {

    // Insert a new category
    @Insert
    suspend fun insertCategory(category: Category)

    // Get all categories
    @Query("SELECT * FROM category")
    suspend fun getAllCategories(): List<Category>

    // Check if a category with the same name already exists
    @Query("SELECT COUNT(*) FROM category WHERE name = :categoryName")
    suspend fun countByName(categoryName: String): Int

    // Delete the latest category with the same name
    @Query("""
        DELETE FROM category 
        WHERE id = (SELECT MAX(id) FROM category WHERE name = :categoryName)
    """)
    suspend fun deleteLatestCategoryByName(categoryName: String)

    // Delete a category by its ID
    @Query("DELETE FROM category WHERE id = :categoryId")
    suspend fun deleteCategoryById(categoryId: Int)
}
