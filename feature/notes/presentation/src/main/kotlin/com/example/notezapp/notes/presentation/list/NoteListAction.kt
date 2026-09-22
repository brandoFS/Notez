package com.example.notezapp.notes.presentation.list

sealed interface NoteListAction {
    data object OnAddNoteClick : NoteListAction
    data object OnUndoDelete : NoteListAction
    data class OnNoteClick(val noteId: String) : NoteListAction
    data class OnDeleteNote(val noteId: String) : NoteListAction
}
