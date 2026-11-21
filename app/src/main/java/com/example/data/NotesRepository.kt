package com.example.data

import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class NotesRepository(private val noteDao: NoteDao, private val folderDao: FolderDao) {

  val allNotes: Flow<List<NoteEntity>> = noteDao.observeAllNotes()
  val allFolders: Flow<List<FolderEntity>> = folderDao.observeAllFolders()

  fun observeNoteById(id: Long): Flow<NoteEntity?> = noteDao.observeNoteById(id)

  suspend fun getNoteById(id: Long): NoteEntity? = noteDao.getNoteById(id)

  suspend fun createNote(
    title: String = "",
    content: String = "",
    folderId: Long? = null,
    isPinned: Boolean = false,
  ): Long {
    val now = System.currentTimeMillis()
    return noteDao.insertNote(
      NoteEntity(
        title = title,
        content = content,
        folderId = folderId,
        isPinned = isPinned,
        createdAt = now,
        updatedAt = now,
      )
    )
  }

  suspend fun saveNote(id: Long, title: String, content: String) {
    noteDao.updateTitleAndContent(
      id = id,
      title = title,
      content = content,
      updatedAt = System.currentTimeMillis(),
    )
  }

  suspend fun renameNote(id: Long, newTitle: String) {
    noteDao.renameNote(id = id, title = newTitle, updatedAt = System.currentTimeMillis())
  }

  suspend fun duplicateNote(id: Long): Long? {
    val original = noteDao.getNoteById(id) ?: return null
    val copyTitle =
      if (original.title.isBlank()) "Untitled (Copy)" else "${original.title} (Copy)"
    val now = System.currentTimeMillis()
    return noteDao.insertNote(
      NoteEntity(
        title = copyTitle,
        content = original.content,
        folderId = original.folderId,
        isPinned = false,
        createdAt = now,
        updatedAt = now,
      )
    )
  }

  suspend fun deleteNoteById(id: Long) = noteDao.deleteNoteById(id)

  suspend fun setPinned(id: Long, isPinned: Boolean) = noteDao.setPinned(id, isPinned)

  suspend fun moveNoteToFolder(id: Long, folderId: Long?) =
    noteDao.moveNoteToFolder(id, folderId)

  suspend fun createFolder(name: String): Long {
    val cleanName = name.trim()
    if (cleanName.isEmpty()) return -1L
    return folderDao.insertFolder(FolderEntity(name = cleanName))
  }

  suspend fun renameFolder(id: Long, name: String) {
    val cleanName = name.trim()
    if (cleanName.isNotEmpty()) {
      folderDao.renameFolder(id, cleanName)
    }
  }

  suspend fun deleteFolder(id: Long) = folderDao.deleteFolderById(id)

  /**
   * Imports exact plain-text bytes as UTF-8 without modifying whitespace, newlines, or Unicode.
   */
  suspend fun importPlainText(
    fileName: String?,
    inputStream: InputStream,
    folderId: Long?,
  ): Long =
    withContext(Dispatchers.IO) {
      val rawText = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
      val derivedTitle =
        fileName
          ?.substringBeforeLast('.')
          ?.takeIf { it.isNotBlank() }
          ?: ""
      createNote(title = derivedTitle, content = rawText, folderId = folderId)
    }

  /**
   * Exports exact plain-text UTF-8 content without modification.
   */
  suspend fun exportPlainText(content: String, outputStream: OutputStream) =
    withContext(Dispatchers.IO) {
      outputStream.bufferedWriter(Charsets.UTF_8).use { writer -> writer.write(content) }
    }
}
