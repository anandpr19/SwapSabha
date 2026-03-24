package com.skillswap.app.data.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
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
    @get:PropertyName("isAvailable") @set:PropertyName("isAvailable")
    var isAvailable: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable {
    /** No-arg constructor required by Firestore deserialization. */
    constructor() : this(skillId = "")

    /** Returns the category as a [SkillCategory] enum, defaulting to OTHER. */
    @Exclude
    fun getSkillCategory(): SkillCategory =
        runCatching { SkillCategory.valueOf(category) }.getOrDefault(SkillCategory.OTHER)

    /** Returns the level as a [SkillLevel] enum, defaulting to BEGINNER. */
    @Exclude
    fun getSkillLevel(): SkillLevel =
        runCatching { SkillLevel.valueOf(level) }.getOrDefault(SkillLevel.BEGINNER)
}

