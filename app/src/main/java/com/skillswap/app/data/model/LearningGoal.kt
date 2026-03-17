package com.skillswap.app.data.model

import com.skillswap.app.utils.GoalPriority
import com.skillswap.app.utils.SkillCategory

/**
 * Represents a skill a user wants to learn.
 * Maps to a Firestore document in the `learningGoals` collection.
 */
data class LearningGoal(
    val goalId: String = "",
    val userId: String = "",
    val skillName: String = "",
    val category: String = SkillCategory.OTHER.name,
    val priority: String = GoalPriority.MEDIUM.name,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    /** No-arg constructor required by Firestore deserialization. */
    constructor() : this(goalId = "")

    fun getSkillCategory(): SkillCategory =
        runCatching { SkillCategory.valueOf(category) }.getOrDefault(SkillCategory.OTHER)

    fun getGoalPriority(): GoalPriority =
        runCatching { GoalPriority.valueOf(priority) }.getOrDefault(GoalPriority.MEDIUM)
}
