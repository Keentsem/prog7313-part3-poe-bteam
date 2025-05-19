package com.example.pocketsafe.ui.auth

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    object FirstTimeUser : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val errorMessage: String) : AuthState()
    object Idle : AuthState()
}