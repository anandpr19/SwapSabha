package com.skillswap.app.data.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.skillswap.app.data.model.LearningGoal
import com.skillswap.app.data.model.Skill
import com.skillswap.app.utils.Constants
import com.skillswap.app.utils.SkillCategory
import kotlinx.coroutines.tasks.await

/** Handles all Firestore operations for the `skills` and `learningGoals` collections. */
class SkillRepository {

    private val db = FirebaseFirestore.getInstance()
    private val skillsCollection = db.collection(Constants.COLLECTION_SKILLS)
    private val goalsCollection = db.collection(Constants.COLLECTION_LEARNING_GOALS)

    // ─── Skill CRUD ──────────────────────────────────────────

    /** Adds a new skill document and returns the generated skill with its ID. */
    suspend fun addSkill(skill: Skill): Result<Skill> {
        return try {
            val docRef = skillsCollection.document()
            val skillWithId = skill.copy(skillId = docRef.id)
            docRef.set(skillWithId).await()
            Result.success(skillWithId)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to add skill: ${e.message}"))
        }
    }

    /** Updates specific fields on an existing skill document. */
    suspend fun updateSkill(skillId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            val withTimestamp = updates.toMutableMap().apply {
                put("updatedAt", System.currentTimeMillis())
            }
            skillsCollection.document(skillId).update(withTimestamp).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to update skill: ${e.message}"))
        }
    }

    /** Deletes a skill document permanently. */
    suspend fun deleteSkill(skillId: String): Result<Unit> {
        return try {
            skillsCollection.document(skillId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to delete skill: ${e.message}"))
        }
    }

    /** Fetches all skills belonging to a specific user. */
    suspend fun getMySkills(userId: String): Result<List<Skill>> {
        return try {
            val snapshot = skillsCollection
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val skills = snapshot.documents.mapNotNull { it.toObject(Skill::class.java) }
            Result.success(skills)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load your skills: ${e.message}"))
        }
    }

    // ─── Discovery / Browse ──────────────────────────────────

    /**
     * Fetches the first page of all available skills for the discovery feed.
     * Pass [lastSnapshot] for subsequent pages.
     */
    suspend fun getAllSkills(lastSnapshot: DocumentSnapshot? = null): Result<List<Skill>> {
        return try {
            var query = skillsCollection
                .whereEqualTo("isAvailable", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(Constants.PAGE_SIZE.toLong())

            if (lastSnapshot != null) {
                query = query.startAfter(lastSnapshot)
            }

            val snapshot = query.get().await()
            val skills = snapshot.documents.mapNotNull { it.toObject(Skill::class.java) }
            Result.success(skills)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load skills: ${e.message}"))
        }
    }

    /**
     * Returns the raw [DocumentSnapshot] list for the last page,
     * used as a cursor for the next paginated call.
     */
    suspend fun getAllSkillsWithSnapshots(
        lastSnapshot: DocumentSnapshot? = null
    ): Result<Pair<List<Skill>, DocumentSnapshot?>> {
        return try {
            var query = skillsCollection
                .whereEqualTo("isAvailable", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(Constants.PAGE_SIZE.toLong())

            if (lastSnapshot != null) query = query.startAfter(lastSnapshot)

            val snapshot = query.get().await()
            val skills = snapshot.documents.mapNotNull { it.toObject(Skill::class.java) }
            val cursor = snapshot.documents.lastOrNull()
            Result.success(Pair(skills, cursor))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load skills: ${e.message}"))
        }
    }

    // ─── Search & Filter ─────────────────────────────────────

    /**
     * Prefix-searches skills by name (case-sensitive, Firestore native approach).
     * For a production app consider Algolia; this works well for MVP.
     */
    suspend fun searchByName(query: String): Result<List<Skill>> {
        return try {
            val normalised = query.trim()
            val end = normalised.dropLast(1) + (normalised.last() + 1)
            val snapshot = skillsCollection
                .whereGreaterThanOrEqualTo("name", normalised)
                .whereLessThan("name", end)
                .whereEqualTo("isAvailable", true)
                .limit(Constants.PAGE_SIZE.toLong())
                .get()
                .await()
            val skills = snapshot.documents.mapNotNull { it.toObject(Skill::class.java) }
            Result.success(skills)
        } catch (e: Exception) {
            Result.failure(Exception("Search failed: ${e.message}"))
        }
    }

    /** Filters all available skills by a given [SkillCategory]. */
    suspend fun filterByCategory(category: SkillCategory): Result<List<Skill>> {
        return try {
            val snapshot = skillsCollection
                .whereEqualTo("category", category.name)
                .whereEqualTo("isAvailable", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(Constants.PAGE_SIZE.toLong())
                .get()
                .await()
            val skills = snapshot.documents.mapNotNull { it.toObject(Skill::class.java) }
            Result.success(skills)
        } catch (e: Exception) {
            Result.failure(Exception("Filter failed: ${e.message}"))
        }
    }

    // ─── Learning Goals ──────────────────────────────────────

    /** Adds a new learning goal for the given user. */
    suspend fun addLearningGoal(goal: LearningGoal): Result<LearningGoal> {
        return try {
            val docRef = goalsCollection.document()
            val goalWithId = goal.copy(goalId = docRef.id)
            docRef.set(goalWithId).await()
            Result.success(goalWithId)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to add learning goal: ${e.message}"))
        }
    }

    /** Fetches all learning goals for the given user. */
    suspend fun getMyGoals(userId: String): Result<List<LearningGoal>> {
        return try {
            val snapshot = goalsCollection
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val goals = snapshot.documents.mapNotNull { it.toObject(LearningGoal::class.java) }
            Result.success(goals)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load learning goals: ${e.message}"))
        }
    }

    /** Deletes a learning goal by ID. */
    suspend fun deleteGoal(goalId: String): Result<Unit> {
        return try {
            goalsCollection.document(goalId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to delete goal: ${e.message}"))
        }
    }
}
