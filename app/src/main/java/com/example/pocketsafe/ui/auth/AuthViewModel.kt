package com.example.pocketsafe.ui.auth

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pocketsafe.MainApplication
import com.example.pocketsafe.data.User
import com.example.pocketsafe.data.dao.UserDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Authentication ViewModel that handles user login and registration
 * Modified to work without Hilt to prevent app crashes
 * Maintains pixel-retro theme styling for UI elements
 */
class AuthViewModel(
    private val userDao: UserDao
) : ViewModel() {
    
    /**
     * Factory for creating AuthViewModel with the UserDao
     */
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                // Create dao here without Hilt
                val database = MainApplication.getDatabase(application)
                return AuthViewModel(database.userDao()) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                Log.d("AuthViewModel", "Attempting to login with email: $email")
                val user = withContext(Dispatchers.IO) {
                    userDao.getUserByEmail(email)
                }

                if (user != null) {
                    Log.d("AuthViewModel", "User found, checking password")
                    if (user.password == password) {
                        Log.d("AuthViewModel", "Password matches, login successful")
                        _authState.value = AuthState.Success("Login successful")
                    } else {
                        Log.d("AuthViewModel", "Password does not match")
                        _authState.value = AuthState.Error("Invalid password")
                    }
                } else {
                    Log.d("AuthViewModel", "User not found")
                    _authState.value = AuthState.Error("User not found")
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Login error: ${e.message}", e)
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                Log.d("AuthViewModel", "Attempting to register with email: $email")
                val existingUser = withContext(Dispatchers.IO) {
                    userDao.getUserByEmail(email)
                }

                if (existingUser != null) {
                    Log.d("AuthViewModel", "Email already registered")
                    _authState.value = AuthState.Error("Email already registered")
                    return@launch
                }

                val newUser = User(email = email, password = password)
                withContext(Dispatchers.IO) {
                    userDao.insert(newUser)
                }
                Log.d("AuthViewModel", "Registration successful")
                _authState.value = AuthState.FirstTimeUser
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Registration error: ${e.message}", e)
                _authState.value = AuthState.Error(e.message ?: "Registration failed")
            }
        }
    }
}