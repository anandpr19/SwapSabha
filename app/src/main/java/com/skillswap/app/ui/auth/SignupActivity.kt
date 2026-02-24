package com.skillswap.app.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.skillswap.app.databinding.ActivitySignupBinding
import com.skillswap.app.ui.viewmodel.AuthState
import com.skillswap.app.ui.viewmodel.AuthViewModel
import com.skillswap.app.utils.hide
import com.skillswap.app.utils.show
import com.skillswap.app.utils.showToast

/** Signup screen — create a new account with name, email, and password. */
class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        setupListeners()
        observeState()
    }

    private fun setupListeners() {
        binding.btnSignUp.setOnClickListener {
            val name = binding.etName.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()
            viewModel.signUp(name, email, password, confirmPassword)
        }

        binding.tvGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun observeState() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.btnSignUp.isEnabled = !isLoading
            if (isLoading) binding.progressBar.show() else binding.progressBar.hide()
        }

        viewModel.nameError.observe(this) { error -> binding.tilName.error = error }

        viewModel.emailError.observe(this) { error -> binding.tilEmail.error = error }

        viewModel.passwordError.observe(this) { error -> binding.tilPassword.error = error }

        viewModel.confirmPasswordError.observe(this) { error ->
            binding.tilConfirmPassword.error = error
        }

        viewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.SignedUp -> {
                    showToast("Account created! Please verify your email.")
                    startActivity(Intent(this, EmailVerificationActivity::class.java))
                    finish()
                }
                is AuthState.Error -> {
                    showToast(state.message)
                }
                else -> {
                    /* Idle — no action */
                }
            }
        }
    }
}
