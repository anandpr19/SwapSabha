package com.skillswap.app.data.model

/** Aggregated statistics for a user profile. Stored as a nested object inside the user document. */
data class UserStats(
        val avgRating: Double = 0.0,
        val ratingCount: Int = 0,
        val uniqueSkillsTaught: Int = 0,
        val uniqueSkillsLearned: Int = 0,
        val completedSwaps: Int = 0,
        val cancelledSwaps: Int = 0,
        val completionRate: Int = 100
) {
    /** No-arg constructor required for Firestore deserialization */
    constructor() : this(0.0, 0, 0, 0, 0, 0, 100)
}
