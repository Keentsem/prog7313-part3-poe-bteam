package com.example.pocketsafe.data.dao

import androidx.room.*
import com.example.pocketsafe.data.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Int): Category?

    @Delete
    suspend fun deleteCategory(category: Category)

    @Update
    suspend fun update(category: Category)
} 