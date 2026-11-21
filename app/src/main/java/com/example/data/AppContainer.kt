package com.example.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppContainer(context: Context) {
  private val appContext = context.applicationContext
  private val database = VellumDatabase.getInstance(appContext)
  val notesRepository = NotesRepository(database.noteDao(), database.folderDao())
  val preferencesRepository = PreferencesRepository(appContext)
  val draftRecoveryStore = DraftRecoveryStore(appContext)
  private val recoveryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  init {
    recoveryScope.launch { recoverPendingDraftIfNeeded() }
  }

  private suspend fun recoverPendingDraftIfNeeded() {
    val draft = draftRecoveryStore.getPendingDraft() ?: return
    val existing = notesRepository.getNoteById(draft.noteId)
    if (existing != null && draft.timestamp >= existing.updatedAt) {
      if (existing.title != draft.title || existing.content != draft.content) {
        notesRepository.saveNote(draft.noteId, draft.title, draft.content)
      }
    }
    draftRecoveryStore.clearDraft(draft.noteId)
  }
}
