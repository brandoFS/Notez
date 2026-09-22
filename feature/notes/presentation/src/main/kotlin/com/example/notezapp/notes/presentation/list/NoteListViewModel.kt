package com.example.notezapp.notes.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notezapp.core.domain.onFailure
import com.example.notezapp.core.domain.onSuccess
import com.example.notezapp.core.presentation.UiText
import com.example.notezapp.core.presentation.toUiText
import com.example.notezapp.notes.domain.Note
import com.example.notezapp.notes.domain.NoteLocalDataSource
import com.example.notezapp.notes.presentation.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class NoteListViewModel(
    private val noteLocalDataSource: NoteLocalDataSource
) : ViewModel() {

    private val _state = MutableStateFlow(NoteListState())
    val state = _state.asStateFlow()

    private val _events = Channel<NoteListEvent>()
    val events = _events.receiveAsFlow()

    private var recentlyDeletedNote: Note? = null

    // Drives the Room query. Kept separate from state.searchQuery, which the text field
    // renders, so that re-querying can't feed back into itself.
    private val searchQuery = MutableStateFlow("")

    init {
        searchQuery
            .flatMapLatest { query -> noteLocalDataSource.getNotes(query) }
            .onEach { notes ->
                _state.update { state ->
                    state.copy(
                        notes = notes.map { it.toNoteUi() },
                        isLoading = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onAction(action: NoteListAction) {
        when (action) {
            NoteListAction.OnAddNoteClick -> viewModelScope.launch {
                _events.send(NoteListEvent.NavigateToEditor(noteId = null))
            }

            is NoteListAction.OnNoteClick -> viewModelScope.launch {
                _events.send(NoteListEvent.NavigateToEditor(noteId = action.noteId))
            }

            is NoteListAction.OnDeleteNote -> deleteNote(action.noteId)

            NoteListAction.OnUndoDelete -> undoDelete()

            is NoteListAction.OnSearchQueryChange -> setSearchQuery(action.query)

            NoteListAction.OnClearSearch -> setSearchQuery("")
        }
    }

    private fun setSearchQuery(query: String) {
        searchQuery.value = query
        _state.update { it.copy(searchQuery = query) }
    }

    private fun deleteNote(noteId: String) {
        viewModelScope.launch {
            // Read the note first so the undo action has something to restore.
            noteLocalDataSource.getNoteById(noteId)
                .onSuccess { note ->
                    noteLocalDataSource.deleteNote(noteId)
                        .onSuccess {
                            recentlyDeletedNote = note
                            _events.send(
                                NoteListEvent.ShowUndoSnackbar(
                                    UiText.StringResource(R.string.note_deleted)
                                )
                            )
                        }
                        .onFailure { error ->
                            _events.send(NoteListEvent.ShowSnackbar(error.toUiText()))
                        }
                }
                .onFailure { error ->
                    _events.send(NoteListEvent.ShowSnackbar(error.toUiText()))
                }
        }
    }

    private fun undoDelete() {
        val note = recentlyDeletedNote ?: return
        recentlyDeletedNote = null
        viewModelScope.launch {
            noteLocalDataSource.upsertNote(note)
                .onFailure { error ->
                    _events.send(NoteListEvent.ShowSnackbar(error.toUiText()))
                }
        }
    }
}
