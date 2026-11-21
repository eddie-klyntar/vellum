package com.example.data

import android.content.Context
import android.content.SharedPreferences

data class PendingDraft(
  val noteId: Long,
  val title: String,
  val content: String,
  val timestamp: Long,
)

/**
 * Synchronous crash-recovery buffer that snapshots active edits immediately so unexpected
 * process termination never loses unsaved characters.
 */
class DraftRecoveryStore(context: Context) {
  private val prefs: SharedPreferences =
    context.applicationContext.getSharedPreferences("vellum_draft_recovery", Context.MODE_PRIVATE)

  fun saveDraftSnapshot(noteId: Long, title: String, content: String, immediate: Boolean = false) {
    val editor =
      prefs
        .edit()
        .putLong(KEY_NOTE_ID, noteId)
        .putString(KEY_TITLE, title)
        .putString(KEY_CONTENT, content)
        .putLong(KEY_TIMESTAMP, System.currentTimeMillis())
    if (immediate) {
      editor.commit()
    } else {
      editor.apply()
    }
  }

  fun getPendingDraft(): PendingDraft? {
    val noteId = prefs.getLong(KEY_NOTE_ID, -1L)
    if (noteId <= 0L) return null
    val title = prefs.getString(KEY_TITLE, "") ?: ""
    val content = prefs.getString(KEY_CONTENT, "") ?: ""
    val timestamp = prefs.getLong(KEY_TIMESTAMP, 0L)
    return PendingDraft(noteId = noteId, title = title, content = content, timestamp = timestamp)
  }

  fun clearDraft(noteId: Long? = null) {
    if (noteId == null || prefs.getLong(KEY_NOTE_ID, -1L) == noteId) {
      prefs.edit().clear().apply()
    }
  }

  private companion object {
    const val KEY_NOTE_ID = "draft_note_id"
    const val KEY_TITLE = "draft_title"
    const val KEY_CONTENT = "draft_content"
    const val KEY_TIMESTAMP = "draft_timestamp"
  }
}
