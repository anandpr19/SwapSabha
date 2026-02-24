package com.skillswap.app.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages local session data using SharedPreferences. Stores user session info for auth state
 * persistence.
 */
class SharedPreferencesManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "skillswap_prefs"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_IS_PROFILE_COMPLETE = "is_profile_complete"
    }

    private val prefs: SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ─── Session Management ──────────────────────────────────

    fun saveSession(userId: String, name: String, email: String) {
        prefs.edit().apply {
            putString(KEY_USER_ID, userId)
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_EMAIL, email)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    // ─── User Info ───────────────────────────────────────────

    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)

    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)

    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)

    // ─── Profile Completeness ────────────────────────────────

    fun setProfileComplete(complete: Boolean) {
        prefs.edit().putBoolean(KEY_IS_PROFILE_COMPLETE, complete).apply()
    }

    fun isProfileComplete(): Boolean = prefs.getBoolean(KEY_IS_PROFILE_COMPLETE, false)

    // ─── Update Individual Fields ────────────────────────────

    fun updateUserName(name: String) {
        prefs.edit().putString(KEY_USER_NAME, name).apply()
    }
}
