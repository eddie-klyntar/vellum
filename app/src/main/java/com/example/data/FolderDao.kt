package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
  @Query("SELECT * FROM folders ORDER BY name COLLATE NOCASE ASC")
  fun observeAllFolders(): Flow<List<FolderEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertFolder(folder: FolderEntity): Long

  @Query("UPDATE folders SET name = :name WHERE id = :id")
  suspend fun renameFolder(id: Long, name: String)

  @Query("DELETE FROM folders WHERE id = :id")
  suspend fun deleteFolderById(id: Long)
}
