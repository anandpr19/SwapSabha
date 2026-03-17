package com.skillswap.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.skillswap.app.data.local.SharedPreferencesManager
import com.skillswap.app.data.model.Skill
import com.skillswap.app.data.repository.SkillRepository
import com.skillswap.app.data.repository.UserRepository
import com.skillswap.app.utils.Constants
import com.skillswap.app.utils.SkillCategory
import com.skillswap.app.utils.SkillLevel
import kotlinx.coroutines.launch

/** ViewModel for managing the current user's own skills. */
class SkillViewModel(application: Application) : AndroidViewModel(application) {

    private val skillRepository = SkillRepository()
    private val userRepository = UserRepository()
    private val prefsManager = SharedPreferencesManager(application)

    // ─── State ───────────────────────────────────────────────

    private val _mySkills = MutableLiveData<List<Skill>>(emptyList())
    val mySkills: LiveData<List<Skill>> = _mySkills

    private val _skillState = MutableLiveData<SkillState>(SkillState.Idle)
    val skillState: LiveData<SkillState> = _skillState

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // ─── Validation Errors ───────────────────────────────────

    private val _nameError = MutableLiveData<String?>()
    val nameError: LiveData<String?> = _nameError

    private val _descError = MutableLiveData<String?>()
    val descError: LiveData<String?> = _descError

    // ─── Load ────────────────────────────────────────────────

    /** Loads all skills belonging to the current logged-in user. */
    fun loadMySkills() {
        val userId = prefsManager.getUserId() ?: return
        _isLoading.value = true
        viewModelScope.launch {
            val result = skillRepository.getMySkills(userId)
            result
                .onSuccess { skills -> _mySkills.value = skills }
                .onFailure { error ->
                    _skillState.value = SkillState.Error(error.message ?: "Failed to load skills")
                }
            _isLoading.value = false
        }
    }

    // ─── Add ─────────────────────────────────────────────────

    fun addSkill(
        name: String,
        category: SkillCategory,
        level: SkillLevel,
        description: String,
        experienceYears: Int,
        hoursPerWeek: Int
    ) {
        if (!validate(name, description)) return

        val userId = prefsManager.getUserId() ?: return
        _isLoading.value = true

        viewModelScope.launch {
            // Fetch user info to denormalise name/photo onto the skill document
            val profileResult = userRepository.getUserProfile(userId)
            val user = profileResult.getOrNull()

            val skill = Skill(
                userId = userId,
                userName = user?.name ?: prefsManager.getUserName() ?: "",
                userProfilePic = user?.profilePictureUrl ?: "",
                name = name.trim(),
                category = category.name,
                level = level.name,
                description = description.trim(),
                experienceYears = experienceYears,
                hoursPerWeek = hoursPerWeek,
                isAvailable = true,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            skillRepository.addSkill(skill)
                .onSuccess { added ->
                    _mySkills.value = listOf(added) + (_mySkills.value ?: emptyList())
                    _skillState.value = SkillState.Added
                }
                .onFailure { error ->
                    _skillState.value = SkillState.Error(error.message ?: "Failed to add skill")
                }
            _isLoading.value = false
        }
    }

    // ─── Update ──────────────────────────────────────────────

    fun updateSkill(
        skillId: String,
        name: String,
        category: SkillCategory,
        level: SkillLevel,
        description: String,
        experienceYears: Int,
        hoursPerWeek: Int,
        isAvailable: Boolean
    ) {
        if (!validate(name, description)) return
        _isLoading.value = true

        viewModelScope.launch {
            val updates = mapOf(
                "name" to name.trim(),
                "category" to category.name,
                "level" to level.name,
                "description" to description.trim(),
                "experienceYears" to experienceYears,
                "hoursPerWeek" to hoursPerWeek,
                "isAvailable" to isAvailable
            )
            skillRepository.updateSkill(skillId, updates)
                .onSuccess {
                    loadMySkills()
                    _skillState.value = SkillState.Updated
                }
                .onFailure { error ->
                    _skillState.value = SkillState.Error(error.message ?: "Failed to update skill")
                }
            _isLoading.value = false
        }
    }

    // ─── Delete ──────────────────────────────────────────────

    fun deleteSkill(skillId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            skillRepository.deleteSkill(skillId)
                .onSuccess {
                    _mySkills.value = _mySkills.value?.filter { it.skillId != skillId }
                    _skillState.value = SkillState.Deleted
                }
                .onFailure { error ->
                    _skillState.value = SkillState.Error(error.message ?: "Failed to delete skill")
                }
            _isLoading.value = false
        }
    }

    // ─── Helpers ─────────────────────────────────────────────

    private fun validate(name: String, description: String): Boolean {
        var valid = true
        if (name.isBlank() || name.length > Constants.SKILL_NAME_MAX_LENGTH) {
            _nameError.value = "Skill name must be 1–${Constants.SKILL_NAME_MAX_LENGTH} characters"
            valid = false
        } else {
            _nameError.value = null
        }
        if (description.length < Constants.SKILL_DESC_MIN_LENGTH ||
                description.length > Constants.SKILL_DESC_MAX_LENGTH) {
            _descError.value =
                "Description must be ${Constants.SKILL_DESC_MIN_LENGTH}–${Constants.SKILL_DESC_MAX_LENGTH} characters"
            valid = false
        } else {
            _descError.value = null
        }
        return valid
    }

    fun clearState() {
        _skillState.value = SkillState.Idle
        _nameError.value = null
        _descError.value = null
    }

    fun getCurrentUserId(): String? = prefsManager.getUserId()
}

/** Sealed class representing the state of skill operations. */
sealed class SkillState {
    data object Idle : SkillState()
    data object Added : SkillState()
    data object Updated : SkillState()
    data object Deleted : SkillState()
    data class Error(val message: String) : SkillState()
}
