package com.example.notezapp.notes.presentation.editor

import com.example.notezapp.core.presentation.UiText

sealed interface NoteEditorEvent {
    data object NavigateBack : NoteEditorEvent
    data class ShowSnackbar(val message: UiText) : NoteEditorEvent
}
