package com.example.notezapp.notes.presentation.list

import androidx.compose.runtime.Stable

@Stable
data class NoteListState(
    val notes: List<NoteUi> = emptyList(),
    val isLoading: Boolean = true
)
