package com.skillswap.app.data.model

/**
 * Represents a user profile in the SwapSabha system. Maps directly to a document in the Firestore
 * `users` collection.
 */
data class User(
        val userId: String = "",
        val email: String = "",
        val name: String = "",
        val bio: String = "",
        val profilePictureUrl: String = "",
        val campus: String = "",
        val joinDate: Long = System.currentTimeMillis(),
        val reputationScore: Int = 0,
        val totalSwaps: Int = 0,
        val totalHours: Double = 0.0,
        val badges: List<String> = emptyList(),
        val isActive: Boolean = true,
        val lastActiveAt: Long = System.currentTimeMillis(),
        val stats: UserStats = UserStats()
) {
    /** No-arg constructor required for Firestore deserialization */
    constructor() : this(userId = "")
}
