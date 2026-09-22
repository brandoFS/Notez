package com.example.notezapp.notes.presentation.list

import com.example.notezapp.core.presentation.UiText

sealed interface NoteListEvent {
    data class NavigateToEditor(val noteId: String?) : NoteListEvent
    data class ShowUndoSnackbar(val message: UiText) : NoteListEvent
    data class ShowSnackbar(val message: UiText) : NoteListEvent
}
