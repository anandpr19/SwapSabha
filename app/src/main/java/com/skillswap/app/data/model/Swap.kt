package com.skillswap.app.data.model

import com.skillswap.app.utils.SwapStatus
import java.io.Serializable

/**
 * Represents a skill-swap session between two users.
 * Maps 1-to-1 with a Firestore document in the `swaps` collection.
 */
data class Swap(
    val swapId: String = "",
    val requesterId: String = "",
    val requesterName: String = "",
    val requesterProfilePic: String = "",
    val teacherId: String = "",
    val teacherName: String = "",
    val teacherProfilePic: String = "",
    val skillId: String = "",
    val skillName: String = "",
    val status: String = SwapStatus.PENDING.name,
    val proposedDate: Long = 0L,
    val duration: Int = 60,           // minutes
    val location: String = "",
    val message: String = "",
    val rejectionReason: String = "",
    val completedAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable {

    /** No-arg constructor required by Firestore deserialization. */
    constructor() : this(swapId = "")

    /** Returns the status as a [SwapStatus] enum, defaulting to PENDING. */
    fun getSwapStatus(): SwapStatus =
        runCatching { SwapStatus.valueOf(status) }.getOrDefault(SwapStatus.PENDING)

    /** True if the swap is in a terminal state (completed, rejected, or cancelled). */
    fun isTerminal(): Boolean = getSwapStatus() in listOf(
        SwapStatus.COMPLETED, SwapStatus.REJECTED, SwapStatus.CANCELLED
    )

    /** True if the given userId can accept/reject (i.e. they are the teacher). */
    fun canRespondTo(userId: String): Boolean =
        teacherId == userId && getSwapStatus() == SwapStatus.PENDING

    /** True if either participant can cancel this swap. */
    fun canCancel(userId: String): Boolean =
        (requesterId == userId || teacherId == userId) &&
                getSwapStatus() in listOf(SwapStatus.PENDING, SwapStatus.ACCEPTED)

    /** True if the swap can be marked as completed. */
    fun canComplete(userId: String): Boolean =
        (requesterId == userId || teacherId == userId) &&
                getSwapStatus() == SwapStatus.ACCEPTED
}
