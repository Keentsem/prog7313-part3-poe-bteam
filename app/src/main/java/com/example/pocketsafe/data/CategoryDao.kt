package com.example.pocketsafe.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<Category>>

    @Insert
    suspend fun insertCategory(category: Category)

    @Query("SELECT name FROM categories WHERE id = :categoryId")
    fun getCategoryName(categoryId: Int): Flow<String>
} 