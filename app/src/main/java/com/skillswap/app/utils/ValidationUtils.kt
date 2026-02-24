package com.skillswap.app.utils

/**
 * Input validation utilities for the application. Centralizes all validation logic as specified in
 * the project spec.
 */
object ValidationUtils {

    private val EMAIL_PATTERN = Regex("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")

    /** Validates email format. */
    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && email.matches(EMAIL_PATTERN)
    }

    /**
     * Validates password strength. Requirements: minimum 8 chars, at least 1 uppercase letter, at
     * least 1 digit.
     */
    fun isValidPassword(password: String): Boolean {
        return password.length >= Constants.PASSWORD_MIN_LENGTH &&
                password.any { it.isUpperCase() } &&
                password.any { it.isDigit() }
    }

    /** Returns a user-friendly password requirement message. */
    fun getPasswordRequirements(): String {
        return "Password must be at least ${Constants.PASSWORD_MIN_LENGTH} characters with 1 uppercase letter and 1 number"
    }

    /** Validates user display name. Must not be empty, max 100 chars, and not all digits. */
    fun isValidName(name: String): Boolean {
        return name.isNotBlank() &&
                name.length <= Constants.NAME_MAX_LENGTH &&
                !name.matches(Regex("^[0-9]+$"))
    }

    /** Validates user bio (optional field). */
    fun isValidBio(bio: String): Boolean {
        return bio.length <= Constants.BIO_MAX_LENGTH
    }

    /** Validates that passwords match for confirmation. */
    fun doPasswordsMatch(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }

    /** Validates campus selection. */
    fun isValidCampus(campus: String): Boolean {
        return campus.isNotBlank()
    }

    /** Returns validation error message for email, or null if valid. */
    fun getEmailError(email: String): String? {
        return when {
            email.isBlank() -> "Email is required"
            !isValidEmail(email) -> "Please enter a valid email"
            else -> null
        }
    }

    /** Returns validation error message for password, or null if valid. */
    fun getPasswordError(password: String): String? {
        return when {
            password.isBlank() -> "Password is required"
            password.length < Constants.PASSWORD_MIN_LENGTH ->
                    "Password must be at least ${Constants.PASSWORD_MIN_LENGTH} characters"
            !password.any { it.isUpperCase() } ->
                    "Password must contain at least 1 uppercase letter"
            !password.any { it.isDigit() } -> "Password must contain at least 1 number"
            else -> null
        }
    }

    /** Returns validation error for name, or null if valid. */
    fun getNameError(name: String): String? {
        return when {
            name.isBlank() -> "Name is required"
            name.length > Constants.NAME_MAX_LENGTH ->
                    "Name must be ${Constants.NAME_MAX_LENGTH} characters or less"
            name.matches(Regex("^[0-9]+$")) -> "Name cannot be all numbers"
            else -> null
        }
    }

    /** Returns validation error for bio, or null if valid. */
    fun getBioError(bio: String): String? {
        return when {
            bio.length > Constants.BIO_MAX_LENGTH ->
                    "Bio must be ${Constants.BIO_MAX_LENGTH} characters or less"
            else -> null
        }
    }
}
