package com.skillswap.app.data.model

import com.skillswap.app.utils.RatingRole
import java.io.Serializable

/**
 * Represents a rating given by one user to another after a completed swap.
 * Maps 1-to-1 with a Firestore document in the `ratings` collection.
 */
data class Rating(
    val ratingId: String = "",
    val swapId: String = "",
    val raterId: String = "",
    val raterName: String = "",
    val ratedUserId: String = "",
    val role: String = RatingRole.LEARNER.name,
    val stars: Int = 5,
    val comment: String = "",
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
) : Serializable {
    /** No-arg constructor required by Firestore deserialization. */
    constructor() : this(ratingId = "")
}
