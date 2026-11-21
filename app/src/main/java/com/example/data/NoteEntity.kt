package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "notes",
  foreignKeys =
    [
      ForeignKey(
        entity = FolderEntity::class,
        parentColumns = ["id"],
        childColumns = ["folderId"],
        onDelete = ForeignKey.SET_NULL,
      )
    ],
  indices = [Index("folderId"), Index("updatedAt"), Index("isPinned")],
)
data class NoteEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0L,
  val title: String = "",
  val content: String = "",
  val folderId: Long? = null,
  val isPinned: Boolean = false,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis(),
)
