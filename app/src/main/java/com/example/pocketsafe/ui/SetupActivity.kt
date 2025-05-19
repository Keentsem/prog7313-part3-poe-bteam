package com.example.pocketsafe.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.pocketsafe.databinding.ActivitySetupBinding
import com.example.pocketsafe.ui.auth.AuthViewModel
import com.example.pocketsafe.ui.auth.AuthState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SetupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySetupBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        observeState()
    }

    private fun setupViews() {
        binding.btnSave.setOnClickListener {
            val monthlyGoal = binding.etMonthlyGoal.text.toString()
            val monthlyIncome = binding.etMonthlyIncome.text.toString()

            if (monthlyGoal.isEmpty() || monthlyIncome.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val goal = monthlyGoal.toDouble()
                val income = monthlyIncome.toDouble()

                if (goal <= 0 || income <= 0) {
                    Toast.makeText(this, "Values must be greater than 0", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // TODO: Save the goals and income to the database
                val intent = Intent(this, MainMenu::class.java)
                startActivity(intent)
                finish()
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authState.collect { state ->
                    when (state) {
                        is AuthState.Loading -> {
                            binding.btnSave.isEnabled = false
                        }
                        is AuthState.Error -> {
                            binding.btnSave.isEnabled = true
                            Toast.makeText(this@SetupActivity, state.errorMessage, Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            binding.btnSave.isEnabled = true
                        }
                    }
                }
            }
        }
    }
} 