package com.example.notezapp.notes.data

import android.database.sqlite.SQLiteFullException
import com.example.notezapp.core.domain.DataError
import com.example.notezapp.core.domain.EmptyResult
import com.example.notezapp.core.domain.Result
import com.example.notezapp.notes.domain.Note
import com.example.notezapp.notes.domain.NoteLocalDataSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomNoteDataSource(
    private val noteDao: NoteDao
) : NoteLocalDataSource {

    override fun getNotes(query: String): Flow<List<Note>> =
        noteDao.observeNotes(query).map { entities -> entities.map { it.toNote() } }

    override suspend fun getNoteById(id: String): Result<Note, DataError.Local> {
        return try {
            val entity = noteDao.getNoteById(id)
                ?: return Result.Error(DataError.Local.NOT_FOUND)
            Result.Success(entity.toNote())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(DataError.Local.UNKNOWN)
        }
    }

    override suspend fun upsertNote(note: Note): EmptyResult<DataError.Local> {
        return try {
            noteDao.upsertNote(note.toNoteEntity())
            Result.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: SQLiteFullException) {
            Result.Error(DataError.Local.DISK_FULL)
        } catch (e: Exception) {
            Result.Error(DataError.Local.UNKNOWN)
        }
    }

    override suspend fun deleteNote(id: String): EmptyResult<DataError.Local> {
        return try {
            noteDao.deleteNote(id)
            Result.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(DataError.Local.UNKNOWN)
        }
    }
}
