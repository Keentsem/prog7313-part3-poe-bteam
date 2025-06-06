package com.example.pocketsafe.ui

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.pocketsafe.MainApplication
import com.example.pocketsafe.R
import com.example.pocketsafe.data.User
import com.example.pocketsafe.data.dao.UserDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Activity to display a list of users with pixel-retro theme styling
 * Modified to work without Hilt to prevent app crashes
 * Uses gold (#f3c34e) and brown (#5b3f2c) color scheme
 */
class UserListActivity : AppCompatActivity() {
    private lateinit var userDao: UserDao
    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)
        
        // Apply pixel-retro theme styling with brown (#5b3f2c) background
        window.decorView.setBackgroundColor(android.graphics.Color.parseColor("#5b3f2c"))
        
        // Manually initialize UserDao without Hilt
        try {
            val database = MainApplication.getDatabase(applicationContext)
            userDao = database.userDao()
            Log.d("UserListActivity", "Database initialized successfully")
        } catch (e: Exception) {
            Log.e("UserListActivity", "Error initializing database: ${e.message}")
            Toast.makeText(this, "Error initializing database: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        try {
            listView = findViewById(R.id.listView)

            lifecycleScope.launch {
                try {
                    // Check if database is empty
                    val users = withContext(Dispatchers.IO) {
                        userDao.getAllUsers()
                    }
                    
                    if (users.isEmpty()) {
                        // Add test data
                        withContext(Dispatchers.IO) {
                            userDao.insert(User(email = "test1@example.com", password = "password1", name = "Test User 1"))
                            userDao.insert(User(email = "test2@example.com", password = "password2", name = "Test User 2"))
                            userDao.insert(User(email = "test3@example.com", password = "password3", name = "Test User 3"))
                        }
                    }
                    
                    // Get updated list of users
                    val updatedUsers = withContext(Dispatchers.IO) {
                        userDao.getAllUsers()
                    }
                    
                    // Update UI
                    val adapter = ArrayAdapter(
                        this@UserListActivity,
                        android.R.layout.simple_list_item_1,
                        updatedUsers.map { it.name }
                    )
                    listView.adapter = adapter
                    
                } catch (e: Exception) {
                    Toast.makeText(this@UserListActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error initializing UI: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
} 