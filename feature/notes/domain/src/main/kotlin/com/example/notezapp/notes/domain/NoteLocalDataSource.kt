package com.example.notezapp.notes.domain

import com.example.notezapp.core.domain.DataError
import com.example.notezapp.core.domain.EmptyResult
import com.example.notezapp.core.domain.Result
import kotlinx.coroutines.flow.Flow

interface NoteLocalDataSource {
    /** [query] matches against title and content; blank returns every note. */
    fun getNotes(query: String = ""): Flow<List<Note>>
    suspend fun getNoteById(id: String): Result<Note, DataError.Local>
    suspend fun upsertNote(note: Note): EmptyResult<DataError.Local>
    suspend fun deleteNote(id: String): EmptyResult<DataError.Local>
}
