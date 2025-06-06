package com.example.pocketsafe.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.pocketsafe.R
import com.example.pocketsafe.databinding.ActivityLoginBinding
import com.example.pocketsafe.ui.MainMenu
import com.example.pocketsafe.ui.SetupActivity
import kotlinx.coroutines.launch

/**
 * Login activity with pixel-retro theme styling
 * Modified to work without Hilt to prevent app crashes
 * Uses gold (#f3c34e) and brown (#5b3f2c) color scheme
 */
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("LoginActivity", "onCreate called")
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Apply pixel-retro theme styling with brown (#5b3f2c) background
        window.decorView.setBackgroundColor(android.graphics.Color.parseColor("#5b3f2c"))
        
        // Initialize ViewModel using Factory pattern instead of Hilt
        try {
            viewModel = ViewModelProvider(this, AuthViewModel.Factory(application))
                .get(AuthViewModel::class.java)
            Log.d("LoginActivity", "ViewModel initialized successfully")
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error initializing ViewModel: ${e.message}")
            Toast.makeText(this, "Error initializing ViewModel: ${e.message}", Toast.LENGTH_LONG).show()
        }

        setupViews()
        observeState()
    }

    private fun setupViews() {
        Log.d("LoginActivity", "Setting up views")
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            Log.d("LoginActivity", "Login button clicked with email: $email")
            viewModel.login(email, password)
        }

        binding.btnRegister.setOnClickListener {
            Log.d("LoginActivity", "Register button clicked")
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authState.collect { state ->
                    when (state) {
                        is AuthState.Loading -> {
                            // Show loading indicator
                            binding.btnLogin.isEnabled = false
                            binding.btnRegister.isEnabled = false
                        }
                        is AuthState.Success -> {
                            // Navigate to MainMenu
                            val intent = Intent(this@LoginActivity, MainMenu::class.java)
                            startActivity(intent)
                            finish()
                        }
                        is AuthState.Error -> {
                            // Show error message
                            Toast.makeText(this@LoginActivity, state.errorMessage, Toast.LENGTH_SHORT).show()
                            binding.btnLogin.isEnabled = true
                            binding.btnRegister.isEnabled = true
                        }
                        is AuthState.FirstTimeUser -> {
                            // Navigate to SetupActivity
                            val intent = Intent(this@LoginActivity, SetupActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        else -> {
                            binding.btnLogin.isEnabled = true
                            binding.btnRegister.isEnabled = true
                        }
                    }
                }
            }
        }
    }
}