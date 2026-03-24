package com.skillswap.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.skillswap.app.data.local.SharedPreferencesManager
import com.skillswap.app.data.model.Swap
import com.skillswap.app.data.repository.SwapRepository
import com.skillswap.app.data.repository.UserRepository
import com.skillswap.app.utils.Constants
import com.skillswap.app.utils.SwapStatus
import kotlinx.coroutines.launch

/**
 * ViewModel for managing swap lifecycle — requests, active swaps, and history.
 */
class SwapViewModel(application: Application) : AndroidViewModel(application) {

    private val swapRepository = SwapRepository()
    private val userRepository = UserRepository()
    private val prefsManager = SharedPreferencesManager(application)

    // ─── State ───────────────────────────────────────────────

    private val _incomingRequests = MutableLiveData<List<Swap>>(emptyList())
    val incomingRequests: LiveData<List<Swap>> = _incomingRequests

    private val _outgoingRequests = MutableLiveData<List<Swap>>(emptyList())
    val outgoingRequests: LiveData<List<Swap>> = _outgoingRequests

    private val _activeSwaps = MutableLiveData<List<Swap>>(emptyList())
    val activeSwaps: LiveData<List<Swap>> = _activeSwaps

    private val _swapHistory = MutableLiveData<List<Swap>>(emptyList())
    val swapHistory: LiveData<List<Swap>> = _swapHistory

    private val _swapState = MutableLiveData<SwapState>(SwapState.Idle)
    val swapState: LiveData<SwapState> = _swapState

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // ─── Validation Errors ───────────────────────────────────

    private val _messageError = MutableLiveData<String?>()
    val messageError: LiveData<String?> = _messageError

    private val _locationError = MutableLiveData<String?>()
    val locationError: LiveData<String?> = _locationError

    // ─── Load ────────────────────────────────────────────────

    /** Loads all swap lists for the current user. */
    fun loadAllSwaps() {
        val userId = prefsManager.getUserId() ?: return
        loadIncoming(userId)
        loadOutgoing(userId)
        loadActive(userId)
        loadHistory(userId)
    }

    fun loadIncoming(userId: String? = null) {
        val uid = userId ?: prefsManager.getUserId() ?: return
        viewModelScope.launch {
            swapRepository.getIncomingRequests(uid)
                .onSuccess { _incomingRequests.value = it }
                .onFailure { _swapState.value = SwapState.Error(it.message ?: "Load failed") }
        }
    }

    fun loadOutgoing(userId: String? = null) {
        val uid = userId ?: prefsManager.getUserId() ?: return
        viewModelScope.launch {
            swapRepository.getOutgoingRequests(uid)
                .onSuccess { _outgoingRequests.value = it }
                .onFailure { _swapState.value = SwapState.Error(it.message ?: "Load failed") }
        }
    }

    fun loadActive(userId: String? = null) {
        val uid = userId ?: prefsManager.getUserId() ?: return
        viewModelScope.launch {
            swapRepository.getActiveSwaps(uid)
                .onSuccess { _activeSwaps.value = it }
                .onFailure { _swapState.value = SwapState.Error(it.message ?: "Load failed") }
        }
    }

    fun loadHistory(userId: String? = null) {
        val uid = userId ?: prefsManager.getUserId() ?: return
        viewModelScope.launch {
            swapRepository.getSwapHistory(uid)
                .onSuccess { _swapHistory.value = it }
                .onFailure { _swapState.value = SwapState.Error(it.message ?: "Load failed") }
        }
    }

    // ─── Create Request ──────────────────────────────────────

    fun createSwapRequest(
        teacherId: String,
        teacherName: String,
        teacherProfilePic: String,
        skillId: String,
        skillName: String,
        proposedDate: Long,
        duration: Int,
        location: String,
        message: String
    ) {
        if (!validateRequest(location, message, proposedDate, duration)) return

        val userId = prefsManager.getUserId() ?: return
        _isLoading.value = true

        viewModelScope.launch {
            val profileResult = userRepository.getUserProfile(userId)
            val user = profileResult.getOrNull()

            val swap = Swap(
                requesterId = userId,
                requesterName = user?.name ?: prefsManager.getUserName() ?: "",
                requesterProfilePic = user?.profilePictureUrl ?: "",
                teacherId = teacherId,
                teacherName = teacherName,
                teacherProfilePic = teacherProfilePic,
                skillId = skillId,
                skillName = skillName,
                status = SwapStatus.PENDING.name,
                proposedDate = proposedDate,
                duration = duration,
                location = location.trim(),
                message = message.trim(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            swapRepository.createSwapRequest(swap)
                .onSuccess { _swapState.value = SwapState.RequestSent }
                .onFailure { _swapState.value = SwapState.Error(it.message ?: "Failed to send request") }
            _isLoading.value = false
        }
    }

    // ─── Actions ─────────────────────────────────────────────

    fun acceptRequest(swapId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            swapRepository.acceptSwap(swapId)
                .onSuccess {
                    _swapState.value = SwapState.Accepted
                    loadAllSwaps()
                }
                .onFailure { _swapState.value = SwapState.Error(it.message ?: "Accept failed") }
            _isLoading.value = false
        }
    }

    fun rejectRequest(swapId: String, reason: String = "") {
        _isLoading.value = true
        viewModelScope.launch {
            swapRepository.rejectSwap(swapId, reason)
                .onSuccess {
                    _swapState.value = SwapState.Rejected
                    loadAllSwaps()
                }
                .onFailure { _swapState.value = SwapState.Error(it.message ?: "Reject failed") }
            _isLoading.value = false
        }
    }

    fun completeSwap(swapId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            swapRepository.completeSwap(swapId)
                .onSuccess {
                    _swapState.value = SwapState.Completed
                    loadAllSwaps()
                }
                .onFailure { _swapState.value = SwapState.Error(it.message ?: "Complete failed") }
            _isLoading.value = false
        }
    }

    fun cancelSwap(swapId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            swapRepository.cancelSwap(swapId)
                .onSuccess {
                    _swapState.value = SwapState.Cancelled
                    loadAllSwaps()
                }
                .onFailure { _swapState.value = SwapState.Error(it.message ?: "Cancel failed") }
            _isLoading.value = false
        }
    }

    // ─── Validation ──────────────────────────────────────────

    private fun validateRequest(
        location: String,
        message: String,
        proposedDate: Long,
        duration: Int
    ): Boolean {
        var valid = true

        if (location.length < Constants.LOCATION_MIN_LENGTH ||
            location.length > Constants.LOCATION_MAX_LENGTH
        ) {
            _locationError.value = "Location must be ${Constants.LOCATION_MIN_LENGTH}–${Constants.LOCATION_MAX_LENGTH} characters"
            valid = false
        } else {
            _locationError.value = null
        }

        if (message.isNotBlank() && message.length > Constants.MESSAGE_MAX_LENGTH) {
            _messageError.value = "Message must be under ${Constants.MESSAGE_MAX_LENGTH} characters"
            valid = false
        } else {
            _messageError.value = null
        }

        if (proposedDate <= System.currentTimeMillis()) {
            _swapState.value = SwapState.Error("Please select a future date and time")
            valid = false
        }

        if (duration < Constants.SWAP_MIN_DURATION || duration > Constants.SWAP_MAX_DURATION) {
            _swapState.value = SwapState.Error(
                "Duration must be ${Constants.SWAP_MIN_DURATION}–${Constants.SWAP_MAX_DURATION} minutes"
            )
            valid = false
        }

        return valid
    }

    fun clearState() {
        _swapState.value = SwapState.Idle
        _messageError.value = null
        _locationError.value = null
    }

    fun getCurrentUserId(): String? = prefsManager.getUserId()
}

/** Sealed class representing swap operation states. */
sealed class SwapState {
    data object Idle : SwapState()
    data object RequestSent : SwapState()
    data object Accepted : SwapState()
    data object Rejected : SwapState()
    data object Completed : SwapState()
    data object Cancelled : SwapState()
    data class Error(val message: String) : SwapState()
}
