package com.example.notezapp.notes.presentation.list

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import com.example.notezapp.notes.presentation.FakeNoteLocalDataSource
import com.example.notezapp.notes.presentation.note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class NoteListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var dataSource: FakeNoteLocalDataSource

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        dataSource = FakeNoteLocalDataSource()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `notes from the data source surface in state`() = runTest {
        dataSource.upsertNote(note("1", title = "Groceries"))
        val viewModel = NoteListViewModel(dataSource)

        viewModel.state.test {
            val state = awaitItem()
            assertThat(state.notes).hasSize(1)
            assertThat(state.notes.first().id).isEqualTo("1")
            assertThat(state.isLoading).isFalse()
        }
    }

    @Test
    fun `clicking a note emits NavigateToEditor with its id`() = runTest {
        val viewModel = NoteListViewModel(dataSource)

        viewModel.events.test {
            viewModel.onAction(NoteListAction.OnNoteClick("42"))
            assertThat(awaitItem()).isEqualTo(NoteListEvent.NavigateToEditor("42"))
        }
    }

    @Test
    fun `clicking add emits NavigateToEditor with a null id`() = runTest {
        val viewModel = NoteListViewModel(dataSource)

        viewModel.events.test {
            viewModel.onAction(NoteListAction.OnAddNoteClick)
            assertThat(awaitItem()).isEqualTo(NoteListEvent.NavigateToEditor(null))
        }
    }

    @Test
    fun `deleting a note removes it and offers an undo`() = runTest {
        dataSource.upsertNote(note("1"))
        val viewModel = NoteListViewModel(dataSource)

        viewModel.events.test {
            viewModel.onAction(NoteListAction.OnDeleteNote("1"))
            assertThat(awaitItem()).isInstanceOf(NoteListEvent.ShowUndoSnackbar::class)
        }
        assertThat(dataSource.currentNotes).isEmpty()
        assertThat(viewModel.state.value.notes).isEmpty()
    }

    @Test
    fun `undo restores the most recently deleted note`() = runTest {
        dataSource.upsertNote(note("1", title = "Groceries"))
        val viewModel = NoteListViewModel(dataSource)

        viewModel.onAction(NoteListAction.OnDeleteNote("1"))
        assertThat(dataSource.currentNotes).isEmpty()

        viewModel.onAction(NoteListAction.OnUndoDelete)

        assertThat(dataSource.currentNotes).hasSize(1)
        assertThat(dataSource.currentNotes.first().title).isEqualTo("Groceries")
    }

    @Test
    fun `a failing delete surfaces an error snackbar and keeps the note`() = runTest {
        dataSource.upsertNote(note("1"))
        val viewModel = NoteListViewModel(dataSource)
        dataSource.shouldReturnError = true

        viewModel.events.test {
            viewModel.onAction(NoteListAction.OnDeleteNote("1"))
            assertThat(awaitItem()).isInstanceOf(NoteListEvent.ShowSnackbar::class)
        }
        assertThat(dataSource.currentNotes).hasSize(1)
    }
}
