package com.skillswap.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.skillswap.app.data.model.Swap
import com.skillswap.app.utils.Constants
import com.skillswap.app.utils.SwapStatus
import kotlinx.coroutines.tasks.await

/**
 * Handles all Firestore operations for the `swaps` collection.
 * Every query is scoped to swaps the current user participates in.
 */
class SwapRepository {

    private val db = FirebaseFirestore.getInstance()
    private val swapsCollection = db.collection(Constants.COLLECTION_SWAPS)

    // ─── Create ──────────────────────────────────────────────

    /** Creates a new swap request and returns the swap with its generated ID. */
    suspend fun createSwapRequest(swap: Swap): Result<Swap> {
        return try {
            val docRef = swapsCollection.document()
            val swapWithId = swap.copy(swapId = docRef.id)
            docRef.set(swapWithId).await()
            Result.success(swapWithId)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to create swap request: ${e.message}"))
        }
    }

    // ─── Status Transitions ──────────────────────────────────

    /** Teacher accepts a pending swap request. */
    suspend fun acceptSwap(swapId: String): Result<Unit> {
        return updateStatus(swapId, SwapStatus.ACCEPTED)
    }

    /** Teacher rejects a pending swap request. */
    suspend fun rejectSwap(swapId: String, reason: String = ""): Result<Unit> {
        return try {
            swapsCollection.document(swapId).update(
                mapOf(
                    "status" to SwapStatus.REJECTED.name,
                    "rejectionReason" to reason,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to reject swap: ${e.message}"))
        }
    }

    /** Either participant marks the swap as completed. */
    suspend fun completeSwap(swapId: String): Result<Unit> {
        return try {
            swapsCollection.document(swapId).update(
                mapOf(
                    "status" to SwapStatus.COMPLETED.name,
                    "completedAt" to System.currentTimeMillis(),
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to complete swap: ${e.message}"))
        }
    }

    /** Either participant cancels the swap. */
    suspend fun cancelSwap(swapId: String): Result<Unit> {
        return updateStatus(swapId, SwapStatus.CANCELLED)
    }

    // ─── Queries ─────────────────────────────────────────────

    /** Pending swaps where the given user is the teacher (incoming). */
    suspend fun getIncomingRequests(userId: String): Result<List<Swap>> {
        return querySwaps("teacherId", userId, SwapStatus.PENDING)
    }

    /** Pending swaps where the given user is the requester (outgoing). */
    suspend fun getOutgoingRequests(userId: String): Result<List<Swap>> {
        return querySwaps("requesterId", userId, SwapStatus.PENDING)
    }

    /** Accepted swaps where the given user is either participant. */
    suspend fun getActiveSwaps(userId: String): Result<List<Swap>> {
        return try {
            val asRequester = swapsCollection
                .whereEqualTo("requesterId", userId)
                .whereEqualTo("status", SwapStatus.ACCEPTED.name)
                .get().await()
            val asTeacher = swapsCollection
                .whereEqualTo("teacherId", userId)
                .whereEqualTo("status", SwapStatus.ACCEPTED.name)
                .get().await()

            val swaps = (asRequester.documents + asTeacher.documents)
                .mapNotNull { it.toObject(Swap::class.java) }
                .distinctBy { it.swapId }
                .sortedByDescending { it.proposedDate }

            Result.success(swaps)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load active swaps: ${e.message}"))
        }
    }

    /** Completed, rejected, or canceled swaps (history). */
    suspend fun getSwapHistory(userId: String): Result<List<Swap>> {
        return try {
            val terminalStatuses = listOf(
                SwapStatus.COMPLETED.name,
                SwapStatus.REJECTED.name,
                SwapStatus.CANCELLED.name
            )
            val asRequester = swapsCollection
                .whereEqualTo("requesterId", userId)
                .whereIn("status", terminalStatuses)
                .get().await()
            val asTeacher = swapsCollection
                .whereEqualTo("teacherId", userId)
                .whereIn("status", terminalStatuses)
                .get().await()

            val swaps = (asRequester.documents + asTeacher.documents)
                .mapNotNull { it.toObject(Swap::class.java) }
                .distinctBy { it.swapId }
                .sortedByDescending { it.updatedAt }

            Result.success(swaps)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load swap history: ${e.message}"))
        }
    }

    // ─── Helpers ─────────────────────────────────────────────

    private suspend fun updateStatus(swapId: String, status: SwapStatus): Result<Unit> {
        return try {
            swapsCollection.document(swapId).update(
                mapOf(
                    "status" to status.name,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to update swap status: ${e.message}"))
        }
    }

    private suspend fun querySwaps(
        field: String,
        userId: String,
        status: SwapStatus
    ): Result<List<Swap>> {
        return try {
            val snapshot = swapsCollection
                .whereEqualTo(field, userId)
                .whereEqualTo("status", status.name)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val swaps = snapshot.documents.mapNotNull { it.toObject(Swap::class.java) }
            Result.success(swaps)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load swaps: ${e.message}"))
        }
    }
}
