package com.example.ui.components

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeFormatter {
  fun formatRelative(timestampMs: Long, nowMs: Long = System.currentTimeMillis()): String {
    if (timestampMs <= 0L) return ""
    val diff = (nowMs - timestampMs).coerceAtLeast(0L)
    val minutes = diff / 60_000L
    val hours = diff / 3_600_000L
    val days = diff / 86_400_000L

    return when {
      minutes < 1L -> "Just now"
      minutes < 60L -> "${minutes}m ago"
      hours < 24L -> "${hours}h ago"
      days == 1L -> "Yesterday"
      days < 7L -> "${days}d ago"
      else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestampMs))
    }
  }
}
