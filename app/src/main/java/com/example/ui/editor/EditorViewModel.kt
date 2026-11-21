package com.example.ui.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.DraftRecoveryStore
import com.example.data.NotesRepository
import com.example.editor.DetectedUrl
import com.example.editor.PlainTextUtilities
import com.example.editor.UndoRedoManager
import java.io.OutputStream
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SaveStatus {
  SAVED,
  SAVING,
}

data class FindState(
  val isVisible: Boolean = false,
  val query: String = "",
  val caseSensitive: Boolean = false,
  val matches: List<IntRange> = emptyList(),
  val activeIndex: Int = -1,
)

data class EditorUiState(
  val noteId: Long = -1L,
  val isLoaded: Boolean = false,
  val title: String = "",
  val textFieldValue: TextFieldValue = TextFieldValue(""),
  val isPinned: Boolean = false,
  val folderId: Long? = null,
  val saveStatus: SaveStatus = SaveStatus.SAVED,
  val canUndo: Boolean = false,
  val canRedo: Boolean = false,
  val wordCount: Int = 0,
  val charCount: Int = 0,
  val findState: FindState = FindState(),
  val detectedUrls: List<DetectedUrl> = emptyList(),
  val createdAt: Long = 0L,
  val updatedAt: Long = 0L,
)

class EditorViewModel(
  private val notesRepository: NotesRepository,
  private val draftRecoveryStore: DraftRecoveryStore,
) : ViewModel() {

  private val _uiState = MutableStateFlow(EditorUiState())
  val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

  private val undoRedoManager = UndoRedoManager()
  private var autoSaveJob: Job? = null
  private var hasUnsavedEdits = false

  fun loadNote(noteId: Long) {
    if (_uiState.value.noteId == noteId && _uiState.value.isLoaded) return
    flushPendingSaveSync()

    viewModelScope.launch {
      val note = notesRepository.getNoteById(noteId) ?: return@launch
      val pending = draftRecoveryStore.getPendingDraft()
      val usePending =
        pending != null && pending.noteId == noteId && pending.timestamp >= note.updatedAt

      val initialTitle = if (usePending) pending!!.title else note.title
      val initialContent = if (usePending) pending!!.content else note.content

      if (usePending) {
        notesRepository.saveNote(noteId, initialTitle, initialContent)
        draftRecoveryStore.clearDraft(noteId)
      }

      undoRedoManager.initialize(initialContent, TextRange(initialContent.length))
      hasUnsavedEdits = false

      _uiState.value =
        EditorUiState(
          noteId = noteId,
          isLoaded = true,
          title = initialTitle,
          textFieldValue = TextFieldValue(initialContent, TextRange(initialContent.length)),
          isPinned = note.isPinned,
          folderId = note.folderId,
          saveStatus = SaveStatus.SAVED,
          canUndo = false,
          canRedo = false,
          wordCount = PlainTextUtilities.countWords(initialContent),
          charCount = initialContent.length,
          findState = FindState(),
          detectedUrls = PlainTextUtilities.extractUrls(initialContent),
          createdAt = note.createdAt,
          updatedAt = note.updatedAt,
        )
    }
  }

  fun onTitleChange(newTitle: String) {
    val state = _uiState.value
    if (!state.isLoaded || state.title == newTitle) return
    hasUnsavedEdits = true
    _uiState.update { it.copy(title = newTitle, saveStatus = SaveStatus.SAVING) }
    draftRecoveryStore.saveDraftSnapshot(state.noteId, newTitle, state.textFieldValue.text)
    scheduleAutoSave()
  }

  fun onContentChange(newValue: TextFieldValue) {
    val state = _uiState.value
    if (!state.isLoaded) return
    val textChanged = newValue.text != state.textFieldValue.text

    undoRedoManager.onTextChanged(newValue)

    if (!textChanged) {
      _uiState.update { it.copy(textFieldValue = newValue) }
      return
    }

    hasUnsavedEdits = true
    val matches =
      if (state.findState.isVisible && state.findState.query.isNotEmpty()) {
        PlainTextUtilities.findMatches(
          newValue.text,
          state.findState.query,
          state.findState.caseSensitive,
        )
      } else {
        emptyList()
      }

    _uiState.update { current ->
      current.copy(
        textFieldValue = newValue,
        saveStatus = SaveStatus.SAVING,
        canUndo = undoRedoManager.canUndo,
        canRedo = undoRedoManager.canRedo,
        wordCount = PlainTextUtilities.countWords(newValue.text),
        charCount = newValue.text.length,
        detectedUrls = PlainTextUtilities.extractUrls(newValue.text),
        findState =
          current.findState.copy(
            matches = matches,
            activeIndex = if (matches.isEmpty()) -1 else 0,
          ),
      )
    }

    draftRecoveryStore.saveDraftSnapshot(state.noteId, state.title, newValue.text)
    scheduleAutoSave()
  }

  fun undo() {
    val restored = undoRedoManager.undo() ?: return
    applyRestoredTextFieldValue(restored)
  }

  fun redo() {
    val restored = undoRedoManager.redo() ?: return
    applyRestoredTextFieldValue(restored)
  }

  private fun applyRestoredTextFieldValue(restored: TextFieldValue) {
    val state = _uiState.value
    hasUnsavedEdits = true
    val matches =
      if (state.findState.isVisible && state.findState.query.isNotEmpty()) {
        PlainTextUtilities.findMatches(
          restored.text,
          state.findState.query,
          state.findState.caseSensitive,
        )
      } else {
        emptyList()
      }

    _uiState.update { current ->
      current.copy(
        textFieldValue = restored,
        saveStatus = SaveStatus.SAVING,
        canUndo = undoRedoManager.canUndo,
        canRedo = undoRedoManager.canRedo,
        wordCount = PlainTextUtilities.countWords(restored.text),
        charCount = restored.text.length,
        detectedUrls = PlainTextUtilities.extractUrls(restored.text),
        findState =
          current.findState.copy(
            matches = matches,
            activeIndex = if (matches.isEmpty()) -1 else 0,
          ),
      )
    }
    draftRecoveryStore.saveDraftSnapshot(state.noteId, state.title, restored.text)
    scheduleAutoSave()
  }

  fun selectAll() {
    _uiState.update { current ->
      current.copy(
        textFieldValue =
          current.textFieldValue.copy(selection = TextRange(0, current.textFieldValue.text.length))
      )
    }
  }

  fun insertTextAtSelection(inserted: String) {
    val current = _uiState.value.textFieldValue
    val start = current.selection.min.coerceIn(0, current.text.length)
    val end = current.selection.max.coerceIn(0, current.text.length)
    val newText = buildString {
      append(current.text, 0, start)
      append(inserted)
      append(current.text, end, current.text.length)
    }
    val newCursor = start + inserted.length
    onContentChange(TextFieldValue(newText, TextRange(newCursor)))
  }

  fun toggleFindBar() {
    _uiState.update { current ->
      val nextVisible = !current.findState.isVisible
      val matches =
        if (nextVisible && current.findState.query.isNotEmpty()) {
          PlainTextUtilities.findMatches(
            current.textFieldValue.text,
            current.findState.query,
            current.findState.caseSensitive,
          )
        } else {
          emptyList()
        }
      current.copy(
        findState =
          current.findState.copy(
            isVisible = nextVisible,
            matches = matches,
            activeIndex = if (matches.isEmpty()) -1 else 0,
          )
      )
    }
  }

  fun updateFindQuery(query: String) {
    val state = _uiState.value
    val matches =
      PlainTextUtilities.findMatches(
        state.textFieldValue.text,
        query,
        state.findState.caseSensitive,
      )
    val nextIdx = if (matches.isEmpty()) -1 else 0
    _uiState.update {
      it.copy(
        findState = it.findState.copy(query = query, matches = matches, activeIndex = nextIdx)
      )
    }
    if (nextIdx >= 0) {
      focusMatch(matches[nextIdx])
    }
  }

  fun toggleCaseSensitive() {
    val state = _uiState.value
    val nextCase = !state.findState.caseSensitive
    val matches =
      PlainTextUtilities.findMatches(state.textFieldValue.text, state.findState.query, nextCase)
    val nextIdx = if (matches.isEmpty()) -1 else 0
    _uiState.update {
      it.copy(
        findState =
          it.findState.copy(caseSensitive = nextCase, matches = matches, activeIndex = nextIdx)
      )
    }
    if (nextIdx >= 0) {
      focusMatch(matches[nextIdx])
    }
  }

  fun nextMatch() {
    val find = _uiState.value.findState
    if (find.matches.isEmpty()) return
    val nextIdx = (find.activeIndex + 1) % find.matches.size
    _uiState.update { it.copy(findState = it.findState.copy(activeIndex = nextIdx)) }
    focusMatch(find.matches[nextIdx])
  }

  fun previousMatch() {
    val find = _uiState.value.findState
    if (find.matches.isEmpty()) return
    val prevIdx = if (find.activeIndex <= 0) find.matches.lastIndex else find.activeIndex - 1
    _uiState.update { it.copy(findState = it.findState.copy(activeIndex = prevIdx)) }
    focusMatch(find.matches[prevIdx])
  }

  private fun focusMatch(range: IntRange) {
    _uiState.update { current ->
      current.copy(
        textFieldValue =
          current.textFieldValue.copy(selection = TextRange(range.first, range.last + 1))
      )
    }
  }

  fun togglePin() {
    val state = _uiState.value
    if (!state.isLoaded) return
    val nextPin = !state.isPinned
    _uiState.update { it.copy(isPinned = nextPin) }
    viewModelScope.launch { notesRepository.setPinned(state.noteId, nextPin) }
  }

  fun moveToFolder(folderId: Long?) {
    val state = _uiState.value
    if (!state.isLoaded) return
    _uiState.update { it.copy(folderId = folderId) }
    viewModelScope.launch { notesRepository.moveNoteToFolder(state.noteId, folderId) }
  }

  fun deleteCurrentNote(onDeleted: () -> Unit) {
    val id = _uiState.value.noteId
    if (id <= 0L) return
    autoSaveJob?.cancel()
    hasUnsavedEdits = false
    draftRecoveryStore.clearDraft(id)
    viewModelScope.launch {
      notesRepository.deleteNoteById(id)
      _uiState.value = EditorUiState()
      onDeleted()
    }
  }

  fun duplicateCurrentNote(onDuplicated: (Long) -> Unit) {
    val id = _uiState.value.noteId
    if (id <= 0L) return
    viewModelScope.launch {
      flushNowSuspend()
      val copyId = notesRepository.duplicateNote(id)
      if (copyId != null) onDuplicated(copyId)
    }
  }

  fun exportPlainText(outputStream: OutputStream, onDone: () -> Unit = {}) {
    val content = _uiState.value.textFieldValue.text
    viewModelScope.launch {
      notesRepository.exportPlainText(content, outputStream)
      onDone()
    }
  }

  fun flushPendingSaveSync() {
    val state = _uiState.value
    if (!state.isLoaded || state.noteId <= 0L) return
    if (hasUnsavedEdits) {
      draftRecoveryStore.saveDraftSnapshot(
        state.noteId,
        state.title,
        state.textFieldValue.text,
        immediate = true,
      )
      autoSaveJob?.cancel()
      viewModelScope.launch { flushNowSuspend() }
    }
  }

  private fun scheduleAutoSave() {
    autoSaveJob?.cancel()
    autoSaveJob =
      viewModelScope.launch {
        delay(250L)
        flushNowSuspend()
      }
  }

  private suspend fun flushNowSuspend() {
    val state = _uiState.value
    if (!state.isLoaded || state.noteId <= 0L) return
    notesRepository.saveNote(state.noteId, state.title, state.textFieldValue.text)
    draftRecoveryStore.clearDraft(state.noteId)
    hasUnsavedEdits = false
    _uiState.update {
      it.copy(saveStatus = SaveStatus.SAVED, updatedAt = System.currentTimeMillis())
    }
  }

  override fun onCleared() {
    val state = _uiState.value
    if (state.isLoaded && state.noteId > 0L && hasUnsavedEdits) {
      draftRecoveryStore.saveDraftSnapshot(
        state.noteId,
        state.title,
        state.textFieldValue.text,
        immediate = true,
      )
    }
    super.onCleared()
  }

  class Factory(
    private val notesRepository: NotesRepository,
    private val draftRecoveryStore: DraftRecoveryStore,
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
      EditorViewModel(notesRepository, draftRecoveryStore) as T
  }
}
