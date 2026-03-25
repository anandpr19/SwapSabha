package com.skillswap.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.skillswap.app.data.local.SharedPreferencesManager
import com.skillswap.app.data.model.Swap
import com.skillswap.app.data.model.User
import com.skillswap.app.data.repository.RatingRepository
import com.skillswap.app.data.repository.SwapRepository
import com.skillswap.app.data.repository.UserRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for the Home dashboard — loads user stats, recent swaps, and leaderboard.
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository()
    private val swapRepository = SwapRepository()
    private val ratingRepository = RatingRepository()
    private val prefsManager = SharedPreferencesManager(application)

    // ─── State ───────────────────────────────────────────────

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    private val _recentSwaps = MutableLiveData<List<Swap>>(emptyList())
    val recentSwaps: LiveData<List<Swap>> = _recentSwaps

    private val _leaderboard = MutableLiveData<List<Map<String, Any>>>(emptyList())
    val leaderboard: LiveData<List<Map<String, Any>>> = _leaderboard

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // ─── Load ────────────────────────────────────────────────

    fun loadDashboard() {
        val userId = prefsManager.getUserId() ?: return
        _isLoading.value = true

        // Load user profile
        viewModelScope.launch {
            userRepository.getUserProfile(userId)
                .onSuccess { _currentUser.value = it }
        }

        // Load recent completed swaps (last 5)
        viewModelScope.launch {
            swapRepository.getSwapHistory(userId)
                .onSuccess { swaps ->
                    _recentSwaps.value = swaps.take(5)
                }
        }

        // Load leaderboard (top 10)
        viewModelScope.launch {
            ratingRepository.getLeaderboard(10)
                .onSuccess { _leaderboard.value = it }
            _isLoading.value = false
        }
    }

    fun getUserName(): String = prefsManager.getUserName() ?: "Student"
    fun getCurrentUserId(): String? = prefsManager.getUserId()
}
