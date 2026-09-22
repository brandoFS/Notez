package com.example.notezapp.notes.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [NoteEntity::class], version = 1)
abstract class NotezDatabase : RoomDatabase() {
    abstract val noteDao: NoteDao
}
