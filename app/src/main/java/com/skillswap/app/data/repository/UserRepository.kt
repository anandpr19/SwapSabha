package com.skillswap.app.data.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.skillswap.app.data.model.User
import com.skillswap.app.utils.Constants
import kotlinx.coroutines.tasks.await

/**
 * Repository handling all Firestore user profile operations and Firebase Cloud Storage for profile
 * pictures.
 */
class UserRepository {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val usersCollection = db.collection(Constants.COLLECTION_USERS)

    /** Creates a new user profile document in Firestore. */
    suspend fun createUserProfile(user: User): Result<Unit> {
        return try {
            usersCollection.document(user.userId).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to create profile: ${e.message}"))
        }
    }

    /** Retrieves a user profile by userId. */
    suspend fun getUserProfile(userId: String): Result<User> {
        return try {
            val doc = usersCollection.document(userId).get().await()
            val user = doc.toObject(User::class.java)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("User profile not found."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load profile: ${e.message}"))
        }
    }

    /**
     * Updates specific fields in a user profile.
     *
     * @param userId The user's document ID.
     * @param updates Map of field names to new values.
     */
    suspend fun updateUserProfile(userId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            // Use set-with-merge so the document is created if it doesn't exist yet
            usersCollection.document(userId).set(updates, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to update profile: ${e.message}"))
        }
    }

    /**
     * Uploads a profile picture to Firebase Cloud Storage and updates the user's profilePictureUrl
     * in Firestore.
     *
     * @param userId The user's ID (used as the filename).
     * @param imageUri The local URI of the image to upload.
     * @return Result containing the download URL string on success.
     */
    suspend fun uploadProfilePicture(userId: String, imageUri: Uri): Result<String> {
        return try {
            val ref = storage.reference.child("${Constants.STORAGE_PROFILE_PICTURES}/$userId.jpg")

            // Upload the file
            ref.putFile(imageUri).await()

            // Get the download URL
            val downloadUrl = ref.downloadUrl.await().toString()

            // Update the Firestore document with the new URL
            usersCollection.document(userId).update("profilePictureUrl", downloadUrl).await()

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to upload photo: ${e.message}"))
        }
    }

    /** Updates the user's last active timestamp. */
    suspend fun updateLastActive(userId: String): Result<Unit> {
        return try {
            usersCollection
                    .document(userId)
                    .update("lastActiveAt", System.currentTimeMillis())
                    .await()
            Result.success(Unit)
        } catch (e: Exception) {
            // Non-critical — silently fail
            Result.failure(e)
        }
    }

    /** Checks if a user profile exists in Firestore. */
    suspend fun doesProfileExist(userId: String): Boolean {
        return try {
            val doc = usersCollection.document(userId).get().await()
            doc.exists()
        } catch (e: Exception) {
            false
        }
    }
}
