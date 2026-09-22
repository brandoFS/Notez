package com.example.notezapp.notes.presentation.editor

data class NoteEditorState(
    val title: String = "",
    val content: String = "",
    val isSaving: Boolean = false,
    val isNewNote: Boolean = true
)
