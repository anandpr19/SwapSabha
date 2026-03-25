package com.skillswap.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.skillswap.app.data.model.Rating
import com.skillswap.app.utils.BadgeType
import com.skillswap.app.utils.Constants
import kotlinx.coroutines.tasks.await

/**
 * Handles Firestore operations for ratings, reputation scoring, and badge evaluation.
 */
class RatingRepository {

    private val db = FirebaseFirestore.getInstance()
    private val ratingsCollection = db.collection(Constants.COLLECTION_RATINGS)
    private val usersCollection = db.collection(Constants.COLLECTION_USERS)

    // ─── Submit ──────────────────────────────────────────────

    /**
     * Submits a rating and updates the rated user's stats + reputation.
     * Returns failure if the user has already rated this swap.
     */
    suspend fun submitRating(rating: Rating): Result<Rating> {
        return try {
            // Check for duplicate rating
            val existing = ratingsCollection
                .whereEqualTo("swapId", rating.swapId)
                .whereEqualTo("raterId", rating.raterId)
                .get().await()
            if (!existing.isEmpty) {
                return Result.failure(Exception("You've already rated this swap"))
            }

            val docRef = ratingsCollection.document()
            val ratingWithId = rating.copy(ratingId = docRef.id)
            docRef.set(ratingWithId).await()

            // Update the rated user's stats
            updateUserStats(rating.ratedUserId)

            Result.success(ratingWithId)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to submit rating: ${e.message}"))
        }
    }

    // ─── Queries ─────────────────────────────────────────────

    /** Fetches all ratings for a given user, ordered by most recent. */
    suspend fun getRatingsForUser(userId: String): Result<List<Rating>> {
        return try {
            val snapshot = ratingsCollection
                .whereEqualTo("ratedUserId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await()
            val ratings = snapshot.documents.mapNotNull { it.toObject(Rating::class.java) }
            Result.success(ratings)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load ratings: ${e.message}"))
        }
    }

    /** Checks if the current user has already rated a specific swap. */
    suspend fun hasRatedSwap(swapId: String, raterId: String): Result<Boolean> {
        return try {
            val snapshot = ratingsCollection
                .whereEqualTo("swapId", swapId)
                .whereEqualTo("raterId", raterId)
                .get().await()
            Result.success(!snapshot.isEmpty)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to check rating: ${e.message}"))
        }
    }

    // ─── Reputation ──────────────────────────────────────────

    /**
     * Recalculates user stats: average rating, rating count, reputation score, and badges.
     */
    suspend fun updateUserStats(userId: String) {
        try {
            // Fetch all ratings for this user
            val ratingsSnapshot = ratingsCollection
                .whereEqualTo("ratedUserId", userId)
                .get().await()
            val ratings = ratingsSnapshot.documents.mapNotNull { it.toObject(Rating::class.java) }

            val ratingCount = ratings.size
            val avgRating = if (ratingCount > 0) {
                ratings.sumOf { it.stars }.toDouble() / ratingCount
            } else 0.0

            // Read current user doc for swap stats
            val userDoc = usersCollection.document(userId).get().await()
            val completedSwaps = userDoc.getLong("stats.completedSwaps")?.toInt() ?: 0
            val cancelledSwaps = userDoc.getLong("stats.cancelledSwaps")?.toInt() ?: 0
            val uniqueSkillsTaught = userDoc.getLong("stats.uniqueSkillsTaught")?.toInt() ?: 0
            val uniqueSkillsLearned = userDoc.getLong("stats.uniqueSkillsLearned")?.toInt() ?: 0

            // Calculate reputation score (0-100)
            val rawScore = (completedSwaps * 2) + (avgRating * 10).toInt() +
                    (uniqueSkillsTaught * 3) - (cancelledSwaps * 5)
            val reputationScore = rawScore.coerceIn(0, 100)

            // Evaluate badges
            val badges = mutableListOf<String>()
            if (ratingCount >= 5 && avgRating >= 4.5) {
                badges.add(BadgeType.TRUSTED_TEACHER.name)
            }
            if (uniqueSkillsLearned >= 5) {
                badges.add(BadgeType.SKILL_COLLECTOR.name)
            }
            if (cancelledSwaps == 0 && completedSwaps >= 3) {
                badges.add(BadgeType.RELIABLE_PARTNER.name)
            }
            if (uniqueSkillsTaught >= 10) {
                badges.add(BadgeType.EXPERT.name)
            }

            // Update user document
            usersCollection.document(userId).update(
                mapOf(
                    "stats.avgRating" to avgRating,
                    "stats.ratingCount" to ratingCount,
                    "reputationScore" to reputationScore,
                    "badges" to badges
                )
            ).await()
        } catch (_: Exception) {
            // Non-critical — stats will be updated on next rating
        }
    }

    // ─── Leaderboard ─────────────────────────────────────────

    /** Fetches top users by reputation score for the leaderboard. */
    suspend fun getLeaderboard(limit: Int = Constants.LEADERBOARD_SIZE): Result<List<Map<String, Any>>> {
        return try {
            val snapshot = usersCollection
                .orderBy("reputationScore", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get().await()

            val users = snapshot.documents.map { doc ->
                mapOf(
                    "userId" to (doc.getString("userId") ?: ""),
                    "name" to (doc.getString("name") ?: ""),
                    "profilePictureUrl" to (doc.getString("profilePictureUrl") ?: ""),
                    "reputationScore" to (doc.getLong("reputationScore")?.toInt() ?: 0),
                    "avgRating" to (doc.getDouble("stats.avgRating") ?: 0.0)
                )
            }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load leaderboard: ${e.message}"))
        }
    }
}
