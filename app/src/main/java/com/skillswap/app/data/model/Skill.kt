package com.skillswap.app.data.model

import com.skillswap.app.utils.SkillCategory
import com.skillswap.app.utils.SkillLevel
import java.io.Serializable

/**
 * Represents a skill that a user can teach or offer to swap.
 * Maps 1-to-1 with a Firestore document in the `skills` collection.
 */
data class Skill(
    val skillId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userProfilePic: String = "",
    val name: String = "",
    val category: String = SkillCategory.OTHER.name,
    val level: String = SkillLevel.BEGINNER.name,
    val description: String = "",
    val experienceYears: Int = 0,
    val hoursPerWeek: Int = 1,
    val isAvailable: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /** No-arg constructor required by Firestore deserialization. */
    constructor() : this(skillId = "")

    /** Returns the category as a [SkillCategory] enum, defaulting to OTHER. */
    fun getSkillCategory(): SkillCategory =
        runCatching { SkillCategory.valueOf(category) }.getOrDefault(SkillCategory.OTHER)

    /** Returns the level as a [SkillLevel] enum, defaulting to BEGINNER. */
    fun getSkillLevel(): SkillLevel =
        runCatching { SkillLevel.valueOf(level) }.getOrDefault(SkillLevel.BEGINNER)
}
