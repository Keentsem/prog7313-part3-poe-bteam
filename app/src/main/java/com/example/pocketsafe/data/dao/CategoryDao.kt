package com.example.pocketsafe.data.dao

import androidx.room.*
import com.example.pocketsafe.data.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    // Returns Flow for observing changes
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<Category>>

    // Asynchronous version for other uses
    @Query("SELECT * FROM categories")
    suspend fun getAllCategoriesAsync(): List<Category>

    @Insert
    suspend fun insertCategory(category: Category): Long

    @Query("SELECT name FROM categories WHERE id = :categoryId")
    suspend fun getCategoryName(categoryId: Int): String?

    @Query("SELECT name FROM categories WHERE id = :categoryId")
    fun getCategoryNameSync(categoryId: Int): String?

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteCategoryById(categoryId: Int)
}