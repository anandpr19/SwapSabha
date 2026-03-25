package com.skillswap.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.skillswap.app.data.local.SharedPreferencesManager
import com.skillswap.app.data.model.Rating
import com.skillswap.app.data.repository.RatingRepository
import com.skillswap.app.data.repository.UserRepository
import com.skillswap.app.utils.Constants
import com.skillswap.app.utils.RatingRole
import kotlinx.coroutines.launch

/**
 * ViewModel for managing ratings — submit, load, and check duplicates.
 */
class RatingViewModel(application: Application) : AndroidViewModel(application) {

    private val ratingRepository = RatingRepository()
    private val userRepository = UserRepository()
    private val prefsManager = SharedPreferencesManager(application)

    // ─── State ───────────────────────────────────────────────

    private val _ratings = MutableLiveData<List<Rating>>(emptyList())
    val ratings: LiveData<List<Rating>> = _ratings

    private val _hasRated = MutableLiveData(false)
    val hasRated: LiveData<Boolean> = _hasRated

    private val _ratingState = MutableLiveData<RatingState>(RatingState.Idle)
    val ratingState: LiveData<RatingState> = _ratingState

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // ─── Submit ──────────────────────────────────────────────

    fun submitRating(
        swapId: String,
        ratedUserId: String,
        role: RatingRole,
        stars: Int,
        comment: String,
        tags: List<String>
    ) {
        if (stars < 1 || stars > 5) {
            _ratingState.value = RatingState.Error("Please select a star rating")
            return
        }
        if (comment.isNotBlank() && (comment.length < Constants.COMMENT_MIN_LENGTH ||
                    comment.length > Constants.COMMENT_MAX_LENGTH)) {
            _ratingState.value = RatingState.Error(
                "Comment must be ${Constants.COMMENT_MIN_LENGTH}–${Constants.COMMENT_MAX_LENGTH} characters"
            )
            return
        }

        val userId = prefsManager.getUserId() ?: return
        _isLoading.value = true

        viewModelScope.launch {
            val profileResult = userRepository.getUserProfile(userId)
            val user = profileResult.getOrNull()

            val rating = Rating(
                swapId = swapId,
                raterId = userId,
                raterName = user?.name ?: prefsManager.getUserName() ?: "",
                ratedUserId = ratedUserId,
                role = role.name,
                stars = stars,
                comment = comment.trim(),
                tags = tags,
                createdAt = System.currentTimeMillis()
            )

            ratingRepository.submitRating(rating)
                .onSuccess { _ratingState.value = RatingState.Submitted }
                .onFailure { _ratingState.value = RatingState.Error(it.message ?: "Failed to submit rating") }
            _isLoading.value = false
        }
    }

    // ─── Load ────────────────────────────────────────────────

    fun loadRatingsForUser(userId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            ratingRepository.getRatingsForUser(userId)
                .onSuccess { _ratings.value = it }
                .onFailure { _ratingState.value = RatingState.Error(it.message ?: "Load failed") }
            _isLoading.value = false
        }
    }

    fun checkIfRated(swapId: String) {
        val userId = prefsManager.getUserId() ?: return
        viewModelScope.launch {
            ratingRepository.hasRatedSwap(swapId, userId)
                .onSuccess { _hasRated.value = it }
        }
    }

    fun clearState() {
        _ratingState.value = RatingState.Idle
    }
}

/** Sealed class for rating operation states. */
sealed class RatingState {
    data object Idle : RatingState()
    data object Submitted : RatingState()
    data class Error(val message: String) : RatingState()
}
