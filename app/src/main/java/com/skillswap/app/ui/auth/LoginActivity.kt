package com.skillswap.app.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.skillswap.app.MainActivity
import com.skillswap.app.databinding.ActivityLoginBinding
import com.skillswap.app.ui.viewmodel.AuthState
import com.skillswap.app.ui.viewmodel.AuthViewModel
import com.skillswap.app.utils.hide
import com.skillswap.app.utils.show
import com.skillswap.app.utils.showToast

/** Login screen — email/password sign-in with validation. */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        setupListeners()
        observeState()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            viewModel.login(email, password)
        }

        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, PasswordResetActivity::class.java))
        }

        binding.tvGoToSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }
    }

    private fun observeState() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.btnLogin.isEnabled = !isLoading
            if (isLoading) binding.progressBar.show() else binding.progressBar.hide()
        }

        viewModel.emailError.observe(this) { error -> binding.tilEmail.error = error }

        viewModel.passwordError.observe(this) { error -> binding.tilPassword.error = error }

        viewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Authenticated -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finishAffinity()
                }
                is AuthState.EmailNotVerified -> {
                    startActivity(Intent(this, EmailVerificationActivity::class.java))
                }
                is AuthState.ProfileIncomplete -> {
                    // Go directly to main for now — profile completion can be added later
                    startActivity(Intent(this, MainActivity::class.java))
                    finishAffinity()
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
