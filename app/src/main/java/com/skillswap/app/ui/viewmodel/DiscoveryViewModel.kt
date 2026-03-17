package com.skillswap.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.skillswap.app.data.model.Skill
import com.skillswap.app.data.repository.SkillRepository
import com.skillswap.app.utils.SkillCategory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** ViewModel for the Discovery and Search screens. */
class DiscoveryViewModel(application: Application) : AndroidViewModel(application) {

    private val skillRepository = SkillRepository()

    // ─── Discovery Feed State ────────────────────────────────

    private val _discoverySkills = MutableLiveData<List<Skill>>(emptyList())
    val discoverySkills: LiveData<List<Skill>> = _discoverySkills

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _hasMore = MutableLiveData(true)
    val hasMore: LiveData<Boolean> = _hasMore

    private val _discoveryError = MutableLiveData<String?>()
    val discoveryError: LiveData<String?> = _discoveryError

    /** Tracks Firestore cursor for pagination. */
    private var lastSnapshot: DocumentSnapshot? = null

    // ─── Active filter state ──────────────────────────────────

    private var activeCategory: SkillCategory? = null
    private var activeQuery: String = ""

    // ─── Search State ────────────────────────────────────────

    private val _searchResults = MutableLiveData<List<Skill>>()
    val searchResults: LiveData<List<Skill>> = _searchResults

    private val _isSearching = MutableLiveData(false)
    val isSearching: LiveData<Boolean> = _isSearching

    /** Debounce job for search so we don't fire on every keystroke. */
    private var searchJob: Job? = null

    // ─── Load / Pagination ───────────────────────────────────

    /** Loads the first page of skills, resetting any existing list. */
    fun loadSkills() {
        lastSnapshot = null
        _hasMore.value = true
        _discoverySkills.value = emptyList()
        loadNextPage()
    }

    /** Loads the next page of skills, appending to the existing list. */
    fun loadNextPage() {
        if (_isLoading.value == true || _hasMore.value == false) return
        _isLoading.value = true

        viewModelScope.launch {
            val result = skillRepository.getAllSkillsWithSnapshots(lastSnapshot)
            result
                .onSuccess { (skills, cursor) ->
                    lastSnapshot = cursor
                    if (skills.isEmpty()) {
                        _hasMore.value = false
                    } else {
                        val current = _discoverySkills.value ?: emptyList()
                        _discoverySkills.value = current + skills
                        _hasMore.value = skills.size >= 20
                    }
                }
                .onFailure { error ->
                    _discoveryError.value = error.message ?: "Failed to load skills"
                }
            _isLoading.value = false
        }
    }

    // ─── Category Filter ─────────────────────────────────────

    /** Filters the discovery list by category. Pass null to show all. */
    fun filterByCategory(category: SkillCategory?) {
        activeCategory = category
        activeQuery = ""

        if (category == null) {
            loadSkills()
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            skillRepository.filterByCategory(category)
                .onSuccess { skills ->
                    _discoverySkills.value = skills
                    _hasMore.value = false        // no pagination for filtered results
                }
                .onFailure { error ->
                    _discoveryError.value = error.message ?: "Filter failed"
                }
            _isLoading.value = false
        }
    }

    // ─── Search ──────────────────────────────────────────────

    /**
     * Debounced search — waits 400ms after the last keystroke before querying.
     * Call with an empty string to clear search and return to browse mode.
     */
    fun searchSkills(query: String) {
        activeQuery = query
        searchJob?.cancel()

        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }

        _isSearching.value = true
        searchJob = viewModelScope.launch {
            delay(400)
            skillRepository.searchByName(query)
                .onSuccess { skills -> _searchResults.value = skills }
                .onFailure { error -> _discoveryError.value = error.message ?: "Search failed" }
            _isSearching.value = false
        }
    }

    fun clearError() {
        _discoveryError.value = null
    }
}
