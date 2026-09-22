package com.example.notezapp.notes.presentation

import kotlinx.serialization.Serializable

@Serializable
data object NotesGraphRoute

@Serializable
data object NoteListRoute

/** [noteId] is null when creating a new note. */
@Serializable
data class NoteEditorRoute(val noteId: String? = null)
