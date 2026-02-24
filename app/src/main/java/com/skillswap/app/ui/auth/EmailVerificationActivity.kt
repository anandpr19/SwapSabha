package com.skillswap.app.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.skillswap.app.MainActivity
import com.skillswap.app.databinding.ActivityEmailVerificationBinding
import com.skillswap.app.ui.viewmodel.AuthState
import com.skillswap.app.ui.viewmodel.AuthViewModel
import com.skillswap.app.utils.hide
import com.skillswap.app.utils.show
import com.skillswap.app.utils.showToast

/**
 * Email verification screen — users land here after signup. They must verify before accessing the
 * app.
 */
class EmailVerificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmailVerificationBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEmailVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        setupListeners()
        observeState()
    }

    private fun setupListeners() {
        binding.btnCheckVerification.setOnClickListener { viewModel.checkEmailVerification() }

        binding.btnResendEmail.setOnClickListener { viewModel.resendVerificationEmail() }
    }

    private fun observeState() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.btnCheckVerification.isEnabled = !isLoading
            binding.btnResendEmail.isEnabled = !isLoading
            if (isLoading) binding.progressBar.show() else binding.progressBar.hide()
        }

        viewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Authenticated -> {
                    showToast("Email verified! Welcome to SwapSabha!")
                    startActivity(Intent(this, MainActivity::class.java))
                    finishAffinity()
                }
                is AuthState.VerificationEmailSent -> {
                    showToast("Verification email sent! Check your inbox.")
                }
                is AuthState.EmailNotVerified -> {
                    showToast("Email not yet verified. Please check your inbox.")
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
