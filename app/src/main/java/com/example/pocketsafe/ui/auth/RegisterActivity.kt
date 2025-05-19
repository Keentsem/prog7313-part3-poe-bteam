package com.example.pocketsafe.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.pocketsafe.R
import com.example.pocketsafe.databinding.ActivityRegisterBinding
import com.example.pocketsafe.ui.SetupActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

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