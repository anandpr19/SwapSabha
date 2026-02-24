package com.skillswap.app.utils

/** Application-wide constants for SwapSabha. */
object Constants {

    // Firestore collection names
    const val COLLECTION_USERS = "users"
    const val COLLECTION_SKILLS = "skills"
    const val COLLECTION_LEARNING_GOALS = "learningGoals"
    const val COLLECTION_SWAPS = "swaps"
    const val COLLECTION_RATINGS = "ratings"
    const val COLLECTION_NOTIFICATIONS = "notifications"

    // Firebase Storage paths
    const val STORAGE_PROFILE_PICTURES = "profiles"

    // Validation limits
    const val NAME_MIN_LENGTH = 1
    const val NAME_MAX_LENGTH = 100
    const val BIO_MAX_LENGTH = 500
    const val PASSWORD_MIN_LENGTH = 8
    const val SKILL_NAME_MAX_LENGTH = 50
    const val SKILL_DESC_MIN_LENGTH = 10
    const val SKILL_DESC_MAX_LENGTH = 1000
    const val COMMENT_MIN_LENGTH = 10
    const val COMMENT_MAX_LENGTH = 500
    const val MESSAGE_MAX_LENGTH = 500
    const val LOCATION_MIN_LENGTH = 5
    const val LOCATION_MAX_LENGTH = 200
    const val SWAP_MIN_DURATION = 30
    const val SWAP_MAX_DURATION = 240
    const val MAX_EXPERIENCE_YEARS = 50
    const val MAX_HOURS_PER_WEEK = 168
    const val SWAP_MAX_FUTURE_DAYS = 90

    // Pagination
    const val PAGE_SIZE = 20
    const val LEADERBOARD_SIZE = 50

    // Reputation tiers
    const val TIER_BEGINNER_MAX = 20
    const val TIER_EMERGING_MAX = 40
    const val TIER_ESTABLISHED_MAX = 60
    const val TIER_EXPERT_MAX = 80
    const val TIER_MASTER_MAX = 100
}

/** Skill categories enum. */
enum class SkillCategory(val displayName: String) {
    MUSIC("Music"),
    TECH("Technology"),
    SPORTS("Sports"),
    LANGUAGES("Languages"),
    ARTS("Arts"),
    OTHER("Other")
}

/** Skill proficiency levels. */
enum class SkillLevel(val displayName: String) {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced")
}

/**
 * Swap status states — follows the state machine: PENDING → ACCEPTED → ACTIVE → COMPLETED
 * ```
 *    ├→ REJECTED
 *    └→ CANCELLED
 * ```
 */
enum class SwapStatus {
    PENDING,
    ACCEPTED,
    ACTIVE,
    COMPLETED,
    REJECTED,
    CANCELLED
}

/** Rating role — who is being rated. */
enum class RatingRole {
    TEACHER,
    LEARNER
}

/** Learning goal priority. */
enum class GoalPriority(val displayName: String) {
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low")
}

/** Badge types. */
enum class BadgeType(val displayName: String, val icon: String, val description: String) {
    TRUSTED_TEACHER("Trusted Teacher", "🌟", "Received 5+ ratings with average 4.5+ stars"),
    SKILL_COLLECTOR("Skill Collector", "🚀", "Learned 5 or more different skills"),
    EXPERT("Expert", "🎓", "Taught the same skill to 10+ people"),
    COMMUNITY_BUILDER("Community Builder", "💫", "Completed 100+ hours of skill teaching"),
    RELIABLE_PARTNER("Reliable Partner", "🤝", "Zero cancelled swaps and 100% completion rate")
}

/** Notification types. */
enum class NotificationType {
    SWAP_REQUEST_RECEIVED,
    SWAP_REQUEST_ACCEPTED,
    SWAP_COMPLETED,
    NEW_REVIEW,
    BADGE_UNLOCKED,
    SWAP_REMINDER
}

/** Positive rating tags. */
enum class PositiveTag(val displayName: String) {
    PATIENT("Patient"),
    CLEAR_EXPLANATION("Clear Explanation"),
    PUNCTUAL("Punctual"),
    ORGANIZED("Organized"),
    ENCOURAGING("Encouraging"),
    KNOWLEDGEABLE("Knowledgeable"),
    FUN("Fun"),
    CREATIVE("Creative")
}

/** Reputation tiers for display. */
enum class ReputationTier(val displayName: String, val minScore: Int, val maxScore: Int) {
    BEGINNER("Beginner", 0, 20),
    EMERGING("Emerging", 21, 40),
    ESTABLISHED("Established", 41, 60),
    EXPERT("Expert", 61, 80),
    MASTER("Master", 81, 100);

    companion object {
        fun fromScore(score: Int): ReputationTier {
            return entries.findLast { score >= it.minScore } ?: BEGINNER
        }
    }
}
