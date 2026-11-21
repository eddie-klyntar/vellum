package com.example.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.EditorSettings
import com.example.data.FolderEntity
import com.example.data.NoteEntity
import com.example.data.NoteSortOption
import com.example.data.NotesRepository
import com.example.data.PreferencesRepository
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
  val allNotesCount: Int = 0,
  val pinnedNotes: List<NoteEntity> = emptyList(),
  val regularNotes: List<NoteEntity> = emptyList(),
  val folders: List<FolderEntity> = emptyList(),
  val selectedFolderId: Long? = null,
  val searchQuery: String = "",
  val sortOption: NoteSortOption = NoteSortOption.MODIFIED_DESC,
  val isGridView: Boolean = false,
  val confirmBeforeDelete: Boolean = true,
) {
  val isEmpty: Boolean
    get() = pinnedNotes.isEmpty() && regularNotes.isEmpty()
}

class HomeViewModel(
  private val notesRepository: NotesRepository,
  private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

  private val searchQuery = MutableStateFlow("")
  private val selectedFolderId = MutableStateFlow<Long?>(null)

  val uiState: StateFlow<HomeUiState> =
    combine(
        notesRepository.allNotes,
        notesRepository.allFolders,
        preferencesRepository.editorSettings,
        searchQuery,
        selectedFolderId,
      ) { notes, folders, settings, query, folderId ->
        val folderFiltered =
          if (folderId == null) notes else notes.filter { it.folderId == folderId }

        val searched =
          if (query.isBlank()) {
            folderFiltered
          } else {
            folderFiltered.filter { note ->
              note.title.contains(query, ignoreCase = true) ||
                note.content.contains(query, ignoreCase = true)
            }
          }

        val sorted = sortNotes(searched, settings.sortOption)
        HomeUiState(
          allNotesCount = notes.size,
          pinnedNotes = sorted.filter { it.isPinned },
          regularNotes = sorted.filterNot { it.isPinned },
          folders = folders,
          selectedFolderId = folderId,
          searchQuery = query,
          sortOption = settings.sortOption,
          isGridView = settings.isGridView,
          confirmBeforeDelete = settings.confirmBeforeDelete,
        )
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(),
      )

  fun setSearchQuery(query: String) {
    searchQuery.value = query
  }

  fun selectFolder(folderId: Long?) {
    selectedFolderId.value = folderId
  }

  fun createNewNote(onCreated: (Long) -> Unit) {
    viewModelScope.launch {
      val id = notesRepository.createNote(folderId = selectedFolderId.value)
      onCreated(id)
    }
  }

  fun renameNote(noteId: Long, newTitle: String) {
    viewModelScope.launch { notesRepository.renameNote(noteId, newTitle) }
  }

  fun duplicateNote(noteId: Long) {
    viewModelScope.launch { notesRepository.duplicateNote(noteId) }
  }

  fun deleteNote(noteId: Long) {
    viewModelScope.launch { notesRepository.deleteNoteById(noteId) }
  }

  fun togglePin(note: NoteEntity) {
    viewModelScope.launch { notesRepository.setPinned(note.id, !note.isPinned) }
  }

  fun moveToFolder(noteId: Long, folderId: Long?) {
    viewModelScope.launch { notesRepository.moveNoteToFolder(noteId, folderId) }
  }

  fun createFolder(name: String, onCreated: (Long) -> Unit = {}) {
    viewModelScope.launch {
      val id = notesRepository.createFolder(name)
      if (id > 0L) onCreated(id)
    }
  }

  fun renameFolder(folderId: Long, newName: String) {
    viewModelScope.launch { notesRepository.renameFolder(folderId, newName) }
  }

  fun deleteFolder(folderId: Long) {
    viewModelScope.launch {
      if (selectedFolderId.value == folderId) {
        selectedFolderId.value = null
      }
      notesRepository.deleteFolder(folderId)
    }
  }

  fun setSortOption(option: NoteSortOption) {
    viewModelScope.launch {
      preferencesRepository.updateEditorSettings { it.copy(sortOption = option) }
    }
  }

  fun toggleGridView() {
    viewModelScope.launch {
      preferencesRepository.updateEditorSettings { it.copy(isGridView = !it.isGridView) }
    }
  }

  fun importPlainText(
    fileName: String?,
    inputStream: InputStream,
    onImported: (Long) -> Unit = {},
  ) {
    viewModelScope.launch {
      val id = notesRepository.importPlainText(fileName, inputStream, selectedFolderId.value)
      onImported(id)
    }
  }

  fun exportPlainText(content: String, outputStream: OutputStream, onDone: () -> Unit = {}) {
    viewModelScope.launch {
      notesRepository.exportPlainText(content, outputStream)
      onDone()
    }
  }

  private fun sortNotes(notes: List<NoteEntity>, option: NoteSortOption): List<NoteEntity> =
    when (option) {
      NoteSortOption.MODIFIED_DESC -> notes.sortedByDescending { it.updatedAt }
      NoteSortOption.CREATED_DESC -> notes.sortedByDescending { it.createdAt }
      NoteSortOption.TITLE_ASC ->
        notes.sortedWith(
          compareBy(String.CASE_INSENSITIVE_ORDER) {
            it.title.ifBlank { it.content.lineSequence().firstOrNull().orEmpty() }
          }
        )
    }

  class Factory(
    private val notesRepository: NotesRepository,
    private val preferencesRepository: PreferencesRepository,
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
      HomeViewModel(notesRepository, preferencesRepository) as T
  }
}
