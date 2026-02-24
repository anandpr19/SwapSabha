package com.skillswap.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

/**
 * Repository handling all Firebase Authentication operations. Returns Result<T> for clean error
 * handling in ViewModels.
 */
class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /** Returns the currently signed-in Firebase user, or null. */
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    /** Checks if a user is currently signed in. */
    fun isUserSignedIn(): Boolean = auth.currentUser != null

    /** Checks if the current user's email is verified. */
    fun isEmailVerified(): Boolean = auth.currentUser?.isEmailVerified == true

    /**
     * Creates a new user account with email and password.
     *
     * @return Result containing the FirebaseUser on success.
     */
    suspend fun signUp(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Account creation failed. Please try again."))
            }
        } catch (e: FirebaseAuthWeakPasswordException) {
            Result.failure(
                    Exception(
                            "Password is too weak. Use at least 8 characters with uppercase and numbers."
                    )
            )
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("Invalid email format. Please check your email."))
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(Exception("This email is already registered. Try logging in instead."))
        } catch (e: Exception) {
            Result.failure(Exception(mapFirebaseError(e)))
        }
    }

    /**
     * Signs in an existing user with email and password.
     *
     * @return Result containing the FirebaseUser on success.
     */
    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Login failed. Please try again."))
            }
        } catch (e: FirebaseAuthInvalidUserException) {
            Result.failure(Exception("No account found with this email."))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("Invalid email or password."))
        } catch (e: Exception) {
            Result.failure(Exception(mapFirebaseError(e)))
        }
    }

    /** Signs out the current user. */
    fun signOut() {
        auth.signOut()
    }

    /** Sends a password reset email to the given address. */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: FirebaseAuthInvalidUserException) {
            Result.failure(Exception("No account found with this email."))
        } catch (e: Exception) {
            Result.failure(Exception(mapFirebaseError(e)))
        }
    }

    /** Sends an email verification to the current user. */
    suspend fun sendEmailVerification(): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("No user is signed in."))
            user.sendEmailVerification().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(mapFirebaseError(e)))
        }
    }

    /**
     * Reloads the current user to refresh their verification status. Call this after the user
     * claims to have verified their email.
     */
    suspend fun reloadUser(): Result<FirebaseUser> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("No user is signed in."))
            user.reload().await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception(mapFirebaseError(e)))
        }
    }

    /** Maps common Firebase exceptions to user-friendly messages. */
    private fun mapFirebaseError(e: Exception): String {
        return when {
            e.message?.contains("NETWORK", ignoreCase = true) == true ->
                    "Network error. Please check your internet connection."
            e.message?.contains("TOO_MANY_REQUESTS", ignoreCase = true) == true ->
                    "Too many attempts. Please try again later."
            e.message?.contains("INVALID_LOGIN", ignoreCase = true) == true ->
                    "Invalid email or password."
            else -> e.message ?: "Something went wrong. Please try again."
        }
    }
}
