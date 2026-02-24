package com.skillswap.app.ui.auth

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.skillswap.app.databinding.ActivityPasswordResetBinding
import com.skillswap.app.ui.viewmodel.AuthState
import com.skillswap.app.ui.viewmodel.AuthViewModel
import com.skillswap.app.utils.hide
import com.skillswap.app.utils.show
import com.skillswap.app.utils.showToast

/** Password reset screen — sends a reset link to the given email. */
class PasswordResetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPasswordResetBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPasswordResetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        setupListeners()
        observeState()
    }

    private fun setupListeners() {
        binding.btnSendReset.setOnClickListener {
            val email = binding.etEmail.text.toString()
            viewModel.sendPasswordReset(email)
        }

        binding.tvBackToLogin.setOnClickListener { finish() }
    }

    private fun observeState() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.btnSendReset.isEnabled = !isLoading
            if (isLoading) binding.progressBar.show() else binding.progressBar.hide()
        }

        viewModel.emailError.observe(this) { error -> binding.tilEmail.error = error }

        viewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.PasswordResetSent -> {
                    showToast("Reset link sent! Check your email.")
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
