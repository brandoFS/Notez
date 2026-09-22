package com.example.notezapp.notes.presentation.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notezapp.core.domain.onFailure
import com.example.notezapp.core.domain.onSuccess
import com.example.notezapp.core.presentation.toUiText
import com.example.notezapp.notes.domain.Note
import com.example.notezapp.notes.domain.NoteLocalDataSource
import java.util.UUID
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val KEY_TITLE = "editor_title"
private const val KEY_CONTENT = "editor_content"
private const val KEY_NOTE_ID = "editor_note_id"

// Matches the NoteEditorRoute property name. Read directly instead of via
// savedStateHandle.toRoute(), which needs android.os.Bundle and so cannot run in JVM tests.
private const val ARG_NOTE_ID = "noteId"

class NoteEditorViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val noteLocalDataSource: NoteLocalDataSource
) : ViewModel() {

    private val existingNoteId: String? = savedStateHandle[ARG_NOTE_ID]

    // Persisted so a brand new note keeps the same id across process death.
    private val noteId: String = savedStateHandle.get<String>(KEY_NOTE_ID)
        ?: (existingNoteId ?: UUID.randomUUID().toString())
            .also { savedStateHandle[KEY_NOTE_ID] = it }

    private val _state = MutableStateFlow(
        NoteEditorState(
            title = savedStateHandle[KEY_TITLE] ?: "",
            content = savedStateHandle[KEY_CONTENT] ?: "",
            isNewNote = existingNoteId == null
        )
    )
    val state = _state.asStateFlow()

    private val _events = Channel<NoteEditorEvent>()
    val events = _events.receiveAsFlow()

    init {
        // A restored draft is newer than what's on disk, so only load when there isn't one.
        if (existingNoteId != null && savedStateHandle.get<String>(KEY_TITLE) == null) {
            loadNote(existingNoteId)
        }
    }

    fun onAction(action: NoteEditorAction) {
        when (action) {
            is NoteEditorAction.OnTitleChange -> {
                savedStateHandle[KEY_TITLE] = action.title
                _state.update { it.copy(title = action.title) }
            }

            is NoteEditorAction.OnContentChange -> {
                savedStateHandle[KEY_CONTENT] = action.content
                _state.update { it.copy(content = action.content) }
            }

            NoteEditorAction.OnSaveClick -> saveNote()

            NoteEditorAction.OnBackClick -> viewModelScope.launch {
                _events.send(NoteEditorEvent.NavigateBack)
            }
        }
    }

    private fun loadNote(id: String) {
        viewModelScope.launch {
            noteLocalDataSource.getNoteById(id)
                .onSuccess { note ->
                    savedStateHandle[KEY_TITLE] = note.title
                    savedStateHandle[KEY_CONTENT] = note.content
                    _state.update { it.copy(title = note.title, content = note.content) }
                }
                .onFailure { error ->
                    _events.send(NoteEditorEvent.ShowSnackbar(error.toUiText()))
                }
        }
    }

    private fun saveNote() {
        val current = _state.value
        if (current.title.isBlank() && current.content.isBlank()) {
            viewModelScope.launch { _events.send(NoteEditorEvent.NavigateBack) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val note = Note(
                id = noteId,
                title = current.title.trim(),
                content = current.content.trim(),
                updatedAt = System.currentTimeMillis()
            )
            noteLocalDataSource.upsertNote(note)
                .onSuccess {
                    _events.send(NoteEditorEvent.NavigateBack)
                }
                .onFailure { error ->
                    _state.update { it.copy(isSaving = false) }
                    _events.send(NoteEditorEvent.ShowSnackbar(error.toUiText()))
                }
        }
    }
}
