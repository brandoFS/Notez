package com.example.notezapp.notes.data

import com.example.notezapp.notes.domain.Note

fun NoteEntity.toNote(): Note = Note(
    id = id,
    title = title,
    content = content,
    updatedAt = updatedAt
)

fun Note.toNoteEntity(): NoteEntity = NoteEntity(
    id = id,
    title = title,
    content = content,
    updatedAt = updatedAt
)
