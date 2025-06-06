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
import com.example.pocketsafe.databinding.ActivityRegisterBinding
import com.example.pocketsafe.ui.SetupActivity
import kotlinx.coroutines.launch

/**
 * Registration activity with pixel-retro theme styling
 * Modified to work without Hilt to prevent app crashes
 * Uses gold (#f3c34e) and brown (#5b3f2c) color scheme
 */
class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Apply pixel-retro theme styling with brown (#5b3f2c) background
        window.decorView.setBackgroundColor(android.graphics.Color.parseColor("#5b3f2c"))
        
        // Initialize ViewModel using Factory pattern instead of Hilt
        try {
            viewModel = ViewModelProvider(this, AuthViewModel.Factory(application))
                .get(AuthViewModel::class.java)
            Log.d("RegisterActivity", "ViewModel initialized successfully")
        } catch (e: Exception) {
            Log.e("RegisterActivity", "Error initializing ViewModel: ${e.message}")
            Toast.makeText(this, "Error initializing ViewModel: ${e.message}", Toast.LENGTH_LONG).show()
        }

        setupViews()
        observeState()
    }

    private fun setupViews() {
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.register(email, password)
        }

        binding.btnLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authState.collect { state ->
                    when (state) {
                        is AuthState.Initial -> {
                            binding.btnRegister.isEnabled = true
                        }
                        is AuthState.Loading -> {
                            binding.btnRegister.isEnabled = false
                        }
                        is AuthState.Success -> {
                            binding.btnRegister.isEnabled = true
                            Toast.makeText(this@RegisterActivity, state.message, Toast.LENGTH_SHORT).show()
                        }
                        is AuthState.Error -> {
                            binding.btnRegister.isEnabled = true
                            Toast.makeText(this@RegisterActivity, state.errorMessage, Toast.LENGTH_SHORT).show()
                        }
                        is AuthState.FirstTimeUser -> {
                            binding.btnRegister.isEnabled = true
                            val intent = Intent(this@RegisterActivity, SetupActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        is AuthState.Idle -> {
                            binding.btnRegister.isEnabled = true
                        }
                    }
                }
            }
        }
    }
}