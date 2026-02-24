package com.skillswap.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.skillswap.app.data.local.SharedPreferencesManager
import com.skillswap.app.data.model.User
import com.skillswap.app.data.repository.AuthRepository
import com.skillswap.app.data.repository.UserRepository
import com.skillswap.app.utils.ValidationUtils
import kotlinx.coroutines.launch

/**
 * ViewModel for authentication screens (Login, Signup, Verification, Reset). Handles validation,
 * auth operations, and Firestore profile creation.
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()
    private val prefsManager = SharedPreferencesManager(application)

    // ─── Auth State ──────────────────────────────────────────

    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState> = _authState

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // ─── Validation Errors ───────────────────────────────────

    private val _emailError = MutableLiveData<String?>()
    val emailError: LiveData<String?> = _emailError

    private val _passwordError = MutableLiveData<String?>()
    val passwordError: LiveData<String?> = _passwordError

    private val _nameError = MutableLiveData<String?>()
    val nameError: LiveData<String?> = _nameError

    private val _confirmPasswordError = MutableLiveData<String?>()
    val confirmPasswordError: LiveData<String?> = _confirmPasswordError

    // ─── Login ───────────────────────────────────────────────

    fun login(email: String, password: String) {
        // Validate inputs
        val emailErr = ValidationUtils.getEmailError(email)
        val passwordErr = if (password.isBlank()) "Password is required" else null

        _emailError.value = emailErr
        _passwordError.value = passwordErr

        if (emailErr != null || passwordErr != null) return

        _isLoading.value = true
        viewModelScope.launch {
            val result = authRepository.signIn(email.trim(), password)
            result
                    .onSuccess { user ->
                        // Check if email is verified
                        if (!authRepository.isEmailVerified()) {
                            _authState.value = AuthState.EmailNotVerified
                        } else {
                            // Check if profile exists
                            val profileExists = userRepository.doesProfileExist(user.uid)
                            if (profileExists) {
                                val profileResult = userRepository.getUserProfile(user.uid)
                                profileResult
                                        .onSuccess { profile ->
                                            prefsManager.saveSession(
                                                    user.uid,
                                                    profile.name,
                                                    profile.email
                                            )
                                            _authState.value = AuthState.Authenticated(user.uid)
                                        }
                                        .onFailure {
                                            prefsManager.saveSession(user.uid, "", email)
                                            _authState.value = AuthState.Authenticated(user.uid)
                                        }
                            } else {
                                prefsManager.saveSession(user.uid, "", email)
                                _authState.value = AuthState.ProfileIncomplete(user.uid)
                            }
                        }
                    }
                    .onFailure { error ->
                        _authState.value = AuthState.Error(error.message ?: "Login failed")
                    }
            _isLoading.value = false
        }
    }

    // ─── Signup ──────────────────────────────────────────────

    fun signUp(name: String, email: String, password: String, confirmPassword: String) {
        // Validate all fields
        val nameErr = ValidationUtils.getNameError(name)
        val emailErr = ValidationUtils.getEmailError(email)
        val passwordErr = ValidationUtils.getPasswordError(password)
        val confirmErr =
                if (!ValidationUtils.doPasswordsMatch(password, confirmPassword)) {
                    "Passwords do not match"
                } else null

        _nameError.value = nameErr
        _emailError.value = emailErr
        _passwordError.value = passwordErr
        _confirmPasswordError.value = confirmErr

        if (nameErr != null || emailErr != null || passwordErr != null || confirmErr != null) return

        _isLoading.value = true
        viewModelScope.launch {
            val result = authRepository.signUp(email.trim(), password)
            result
                    .onSuccess { firebaseUser ->
                        // Create Firestore profile
                        val user =
                                User(
                                        userId = firebaseUser.uid,
                                        email = email.trim(),
                                        name = name.trim(),
                                        joinDate = System.currentTimeMillis()
                                )
                        userRepository.createUserProfile(user)

                        // Send email verification
                        authRepository.sendEmailVerification()

                        prefsManager.saveSession(firebaseUser.uid, name.trim(), email.trim())
                        _authState.value = AuthState.SignedUp(firebaseUser.uid)
                    }
                    .onFailure { error ->
                        _authState.value = AuthState.Error(error.message ?: "Signup failed")
                    }
            _isLoading.value = false
        }
    }

    // ─── Email Verification ──────────────────────────────────

    fun resendVerificationEmail() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = authRepository.sendEmailVerification()
            result.onSuccess { _authState.value = AuthState.VerificationEmailSent }.onFailure {
                    error ->
                _authState.value =
                        AuthState.Error(error.message ?: "Failed to send verification email")
            }
            _isLoading.value = false
        }
    }

    fun checkEmailVerification() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = authRepository.reloadUser()
            result
                    .onSuccess { user ->
                        if (user.isEmailVerified) {
                            prefsManager.saveSession(
                                    user.uid,
                                    prefsManager.getUserName() ?: "",
                                    user.email ?: ""
                            )
                            _authState.value = AuthState.Authenticated(user.uid)
                        } else {
                            _authState.value = AuthState.EmailNotVerified
                        }
                    }
                    .onFailure { error ->
                        _authState.value =
                                AuthState.Error(error.message ?: "Verification check failed")
                    }
            _isLoading.value = false
        }
    }

    // ─── Password Reset ──────────────────────────────────────

    fun sendPasswordReset(email: String) {
        val emailErr = ValidationUtils.getEmailError(email)
        _emailError.value = emailErr
        if (emailErr != null) return

        _isLoading.value = true
        viewModelScope.launch {
            val result = authRepository.sendPasswordResetEmail(email.trim())
            result.onSuccess { _authState.value = AuthState.PasswordResetSent }.onFailure { error ->
                _authState.value = AuthState.Error(error.message ?: "Failed to send reset email")
            }
            _isLoading.value = false
        }
    }

    // ─── Logout ──────────────────────────────────────────────

    fun logout() {
        authRepository.signOut()
        prefsManager.clearSession()
        _authState.value = AuthState.Idle
    }

    // ─── Utilities ───────────────────────────────────────────

    fun isLoggedIn(): Boolean = authRepository.isUserSignedIn()

    fun getCurrentUserId(): String? = authRepository.getCurrentUser()?.uid

    fun clearErrors() {
        _emailError.value = null
        _passwordError.value = null
        _nameError.value = null
        _confirmPasswordError.value = null
        _authState.value = AuthState.Idle
    }
}

/** Sealed class representing all possible authentication states. */
sealed class AuthState {
    data object Idle : AuthState()
    data class Authenticated(val userId: String) : AuthState()
    data class SignedUp(val userId: String) : AuthState()
    data class ProfileIncomplete(val userId: String) : AuthState()
    data object EmailNotVerified : AuthState()
    data object VerificationEmailSent : AuthState()
    data object PasswordResetSent : AuthState()
    data class Error(val message: String) : AuthState()
}
