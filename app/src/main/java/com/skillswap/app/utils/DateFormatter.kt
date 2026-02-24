package com.skillswap.app.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/** Utility for formatting timestamps for display. */
object DateFormatter {

    private val fullDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val shortDateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    /** Formats a timestamp to a full date string (e.g., "Jan 15, 2026"). */
    fun formatDate(timestamp: Long): String {
        return fullDateFormat.format(Date(timestamp))
    }

    /** Formats a timestamp to a short date (e.g., "Jan 15"). */
    fun formatShortDate(timestamp: Long): String {
        return shortDateFormat.format(Date(timestamp))
    }

    /** Formats a timestamp to date and time (e.g., "Jan 15, 2026 at 02:30 PM"). */
    fun formatDateTime(timestamp: Long): String {
        return dateTimeFormat.format(Date(timestamp))
    }

    /** Formats a timestamp to time only (e.g., "02:30 PM"). */
    fun formatTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }

    /** Returns a human-readable relative time (e.g., "2 hours ago", "3 days ago"). */
    fun getRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            diff < TimeUnit.HOURS.toMillis(1) -> {
                val mins = TimeUnit.MILLISECONDS.toMinutes(diff)
                "$mins ${if (mins == 1L) "minute" else "minutes"} ago"
            }
            diff < TimeUnit.DAYS.toMillis(1) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(diff)
                "$hours ${if (hours == 1L) "hour" else "hours"} ago"
            }
            diff < TimeUnit.DAYS.toMillis(7) -> {
                val days = TimeUnit.MILLISECONDS.toDays(diff)
                "$days ${if (days == 1L) "day" else "days"} ago"
            }
            diff < TimeUnit.DAYS.toMillis(30) -> {
                val weeks = TimeUnit.MILLISECONDS.toDays(diff) / 7
                "$weeks ${if (weeks == 1L) "week" else "weeks"} ago"
            }
            else -> formatDate(timestamp)
        }
    }

    /** Formats a duration in minutes to readable format (e.g., "1h 30m"). */
    fun formatDuration(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return when {
            hours == 0 -> "${mins}m"
            mins == 0 -> "${hours}h"
            else -> "${hours}h ${mins}m"
        }
    }
}
