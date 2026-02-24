package com.skillswap.app.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.skillswap.app.data.local.SharedPreferencesManager
import com.skillswap.app.data.model.User
import com.skillswap.app.data.repository.UserRepository
import com.skillswap.app.utils.ValidationUtils
import kotlinx.coroutines.launch

/** ViewModel for viewing and editing user profiles. */
class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository()
    private val prefsManager = SharedPreferencesManager(application)

    // ─── Profile State ───────────────────────────────────────

    private val _userProfile = MutableLiveData<User?>()
    val userProfile: LiveData<User?> = _userProfile

    private val _profileState = MutableLiveData<ProfileState>(ProfileState.Idle)
    val profileState: LiveData<ProfileState> = _profileState

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // ─── Validation Errors ───────────────────────────────────

    private val _nameError = MutableLiveData<String?>()
    val nameError: LiveData<String?> = _nameError

    private val _bioError = MutableLiveData<String?>()
    val bioError: LiveData<String?> = _bioError

    // ─── Load Profile ────────────────────────────────────────

    fun loadProfile(userId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = userRepository.getUserProfile(userId)
            result
                    .onSuccess { user ->
                        _userProfile.value = user
                        _profileState.value = ProfileState.Loaded
                    }
                    .onFailure { error ->
                        _profileState.value =
                                ProfileState.Error(error.message ?: "Failed to load profile")
                    }
            _isLoading.value = false
        }
    }

    /** Loads the currently logged-in user's profile. */
    fun loadCurrentUserProfile() {
        val userId = prefsManager.getUserId()
        if (userId != null) {
            loadProfile(userId)
        } else {
            _profileState.value = ProfileState.Error("User not logged in")
        }
    }

    // ─── Update Profile ──────────────────────────────────────

    fun updateProfile(name: String, bio: String, campus: String) {
        val nameErr = ValidationUtils.getNameError(name)
        val bioErr = ValidationUtils.getBioError(bio)

        _nameError.value = nameErr
        _bioError.value = bioErr

        if (nameErr != null || bioErr != null) return

        val userId = prefsManager.getUserId() ?: return

        _isLoading.value = true
        viewModelScope.launch {
            val updates =
                    mutableMapOf<String, Any>(
                            "name" to name.trim(),
                            "bio" to bio.trim(),
                            "campus" to campus.trim(),
                            "lastActiveAt" to System.currentTimeMillis()
                    )

            val result = userRepository.updateUserProfile(userId, updates)
            result
                    .onSuccess {
                        prefsManager.updateUserName(name.trim())
                        // Reload profile to reflect changes
                        loadProfile(userId)
                        _profileState.value = ProfileState.Updated
                    }
                    .onFailure { error ->
                        _profileState.value =
                                ProfileState.Error(error.message ?: "Failed to update profile")
                    }
            _isLoading.value = false
        }
    }

    // ─── Upload Profile Picture ──────────────────────────────

    fun uploadProfilePicture(imageUri: Uri) {
        val userId = prefsManager.getUserId() ?: return

        _isLoading.value = true
        viewModelScope.launch {
            val result = userRepository.uploadProfilePicture(userId, imageUri)
            result
                    .onSuccess { url ->
                        _profileState.value = ProfileState.PictureUploaded(url)
                        // Reload profile to get updated picture URL
                        loadProfile(userId)
                    }
                    .onFailure { error ->
                        _profileState.value =
                                ProfileState.Error(error.message ?: "Failed to upload photo")
                    }
            _isLoading.value = false
        }
    }

    // ─── Helpers ─────────────────────────────────────────────

    fun getCurrentUserId(): String? = prefsManager.getUserId()

    fun clearErrors() {
        _nameError.value = null
        _bioError.value = null
        _profileState.value = ProfileState.Idle
    }
}

/** Sealed class representing profile screen states. */
sealed class ProfileState {
    data object Idle : ProfileState()
    data object Loaded : ProfileState()
    data object Updated : ProfileState()
    data class PictureUploaded(val url: String) : ProfileState()
    data class Error(val message: String) : ProfileState()
}
