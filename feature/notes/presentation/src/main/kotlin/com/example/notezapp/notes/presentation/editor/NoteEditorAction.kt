package com.example.notezapp.notes.presentation.editor

sealed interface NoteEditorAction {
    data object OnSaveClick : NoteEditorAction
    data object OnBackClick : NoteEditorAction
    data class OnTitleChange(val title: String) : NoteEditorAction
    data class OnContentChange(val content: String) : NoteEditorAction
}
