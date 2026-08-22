package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeUtils {
    // 12-hour format with single 'h' (no leading zero, e.g. 8:00 AM not 08:00 AM)
    private val timeOnlyFormatter = SimpleDateFormat("h:mm a", Locale.ENGLISH)
    private val dateTimeFormatter = SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.ENGLISH)
    private val clockFormatter = SimpleDateFormat("EEEE, MMMM d, yyyy · h:mm:ss a", Locale.ENGLISH)
    private val shortDateFormatter = SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH)

    fun formatTime12Hour(timestamp: Long): String {
        return timeOnlyFormatter.format(Date(timestamp))
    }

    fun formatPostDateTime(timestamp: Long): String {
        return dateTimeFormatter.format(Date(timestamp))
    }

    fun formatLiveClock(date: Date = Date()): String {
        return clockFormatter.format(date)
    }

    fun formatShortDate(timestamp: Long): String {
        return shortDateFormatter.format(Date(timestamp))
    }

    fun parseSecondsToTimeString(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return String.format(Locale.getDefault(), "%d:%02d", mins, secs)
    }

    fun parseTimeStringToSeconds(timeStr: String): Int {
        val trimmed = timeStr.trim()
        if (trimmed.isBlank()) return 0
        if (trimmed.contains(":")) {
            val parts = trimmed.split(":")
            if (parts.size == 2) {
                val mins = parts[0].trim().toIntOrNull() ?: 0
                val secs = parts[1].trim().toIntOrNull() ?: 0
                return (mins * 60) + secs
            }
        }
        return trimmed.toIntOrNull() ?: 0
    }
}
