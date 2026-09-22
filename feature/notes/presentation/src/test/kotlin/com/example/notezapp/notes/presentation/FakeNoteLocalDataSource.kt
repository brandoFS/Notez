package com.example.notezapp.notes.presentation

import com.example.notezapp.core.domain.DataError
import com.example.notezapp.core.domain.EmptyResult
import com.example.notezapp.core.domain.Result
import com.example.notezapp.notes.domain.Note
import com.example.notezapp.notes.domain.NoteLocalDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeNoteLocalDataSource : NoteLocalDataSource {

    private val notes = MutableStateFlow<List<Note>>(emptyList())

    var shouldReturnError = false

    val currentNotes: List<Note> get() = notes.value

    override fun getNotes(): Flow<List<Note>> = notes

    override suspend fun getNoteById(id: String): Result<Note, DataError.Local> {
        if (shouldReturnError) return Result.Error(DataError.Local.UNKNOWN)
        val note = notes.value.firstOrNull { it.id == id }
            ?: return Result.Error(DataError.Local.NOT_FOUND)
        return Result.Success(note)
    }

    override suspend fun upsertNote(note: Note): EmptyResult<DataError.Local> {
        if (shouldReturnError) return Result.Error(DataError.Local.DISK_FULL)
        notes.update { current -> current.filterNot { it.id == note.id } + note }
        return Result.Success(Unit)
    }

    override suspend fun deleteNote(id: String): EmptyResult<DataError.Local> {
        if (shouldReturnError) return Result.Error(DataError.Local.UNKNOWN)
        notes.update { current -> current.filterNot { it.id == id } }
        return Result.Success(Unit)
    }
}

fun note(
    id: String,
    title: String = "Title $id",
    content: String = "Content $id",
    updatedAt: Long = 1_758_500_000_000
) = Note(id = id, title = title, content = content, updatedAt = updatedAt)
