package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
  @Query("SELECT * FROM notes ORDER BY isPinned DESC, updatedAt DESC")
  fun observeAllNotes(): Flow<List<NoteEntity>>

  @Query("SELECT * FROM notes WHERE id = :id")
  fun observeNoteById(id: Long): Flow<NoteEntity?>

  @Query("SELECT * FROM notes WHERE id = :id")
  suspend fun getNoteById(id: Long): NoteEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertNote(note: NoteEntity): Long

  @Update
  suspend fun updateNote(note: NoteEntity)

  @Query("UPDATE notes SET title = :title, content = :content, updatedAt = :updatedAt WHERE id = :id")
  suspend fun updateTitleAndContent(id: Long, title: String, content: String, updatedAt: Long)

  @Query("UPDATE notes SET title = :title, updatedAt = :updatedAt WHERE id = :id")
  suspend fun renameNote(id: Long, title: String, updatedAt: Long)

  @Query("UPDATE notes SET isPinned = :isPinned WHERE id = :id")
  suspend fun setPinned(id: Long, isPinned: Boolean)

  @Query("UPDATE notes SET folderId = :folderId WHERE id = :id")
  suspend fun moveNoteToFolder(id: Long, folderId: Long?)

  @Query("DELETE FROM notes WHERE id = :id")
  suspend fun deleteNoteById(id: Long)
}
