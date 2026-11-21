package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [NoteEntity::class, FolderEntity::class], version = 1, exportSchema = false)
abstract class VellumDatabase : RoomDatabase() {
  abstract fun noteDao(): NoteDao

  abstract fun folderDao(): FolderDao

  companion object {
    @Volatile private var INSTANCE: VellumDatabase? = null

    fun getInstance(context: Context): VellumDatabase {
      return INSTANCE
        ?: synchronized(this) {
          val instance =
            Room.databaseBuilder(
                context.applicationContext,
                VellumDatabase::class.java,
                "vellum_notepad.db",
              )
              .build()
          INSTANCE = instance
          instance
        }
    }
  }
}
