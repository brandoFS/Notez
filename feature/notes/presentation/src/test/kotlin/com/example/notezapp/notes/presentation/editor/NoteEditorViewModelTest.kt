package com.example.notezapp.notes.presentation.editor

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
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

class NoteEditorViewModelTest {

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

    private fun viewModel(noteId: String? = null, vararg extras: Pair<String, Any?>) =
        NoteEditorViewModel(
            savedStateHandle = SavedStateHandle(mapOf("noteId" to noteId) + extras),
            noteLocalDataSource = dataSource
        )

    @Test
    fun `a new note starts empty and is flagged as new`() = runTest {
        val state = viewModel().state.value

        assertThat(state.title).isEqualTo("")
        assertThat(state.content).isEqualTo("")
        assertThat(state.isNewNote).isTrue()
    }

    @Test
    fun `an existing note is loaded into state`() = runTest {
        dataSource.upsertNote(note("1", title = "Groceries", content = "Oat milk"))

        val state = viewModel(noteId = "1").state.value

        assertThat(state.title).isEqualTo("Groceries")
        assertThat(state.content).isEqualTo("Oat milk")
        assertThat(state.isNewNote).isFalse()
    }

    @Test
    fun `editing the title and content updates state`() = runTest {
        val viewModel = viewModel()

        viewModel.onAction(NoteEditorAction.OnTitleChange("Shopping"))
        viewModel.onAction(NoteEditorAction.OnContentChange("Lemons"))

        assertThat(viewModel.state.value.title).isEqualTo("Shopping")
        assertThat(viewModel.state.value.content).isEqualTo("Lemons")
    }

    @Test
    fun `saving persists the note and navigates back`() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(NoteEditorAction.OnTitleChange("Shopping"))
        viewModel.onAction(NoteEditorAction.OnContentChange("Lemons"))

        viewModel.events.test {
            viewModel.onAction(NoteEditorAction.OnSaveClick)
            assertThat(awaitItem()).isEqualTo(NoteEditorEvent.NavigateBack)
        }

        assertThat(dataSource.currentNotes).hasSize(1)
        assertThat(dataSource.currentNotes.first().title).isEqualTo("Shopping")
    }

    @Test
    fun `saving an existing note updates it in place rather than adding a second`() = runTest {
        dataSource.upsertNote(note("1", title = "Groceries"))
        val viewModel = viewModel(noteId = "1")

        viewModel.onAction(NoteEditorAction.OnTitleChange("Groceries v2"))
        viewModel.onAction(NoteEditorAction.OnSaveClick)

        assertThat(dataSource.currentNotes).hasSize(1)
        assertThat(dataSource.currentNotes.first().title).isEqualTo("Groceries v2")
    }

    @Test
    fun `saving a blank note discards it and navigates back`() = runTest {
        val viewModel = viewModel()

        viewModel.events.test {
            viewModel.onAction(NoteEditorAction.OnSaveClick)
            assertThat(awaitItem()).isEqualTo(NoteEditorEvent.NavigateBack)
        }

        assertThat(dataSource.currentNotes).isEmpty()
    }

    @Test
    fun `a draft restored from SavedStateHandle wins over the stored note`() = runTest {
        dataSource.upsertNote(note("1", title = "Groceries", content = "Oat milk"))

        val viewModel = viewModel(
            noteId = "1",
            "editor_title" to "Groceries (edited)",
            "editor_content" to "Oat milk, lemons"
        )

        assertThat(viewModel.state.value.title).isEqualTo("Groceries (edited)")
        assertThat(viewModel.state.value.content).isEqualTo("Oat milk, lemons")
    }

    @Test
    fun `a failing save keeps the user on the screen with an error`() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(NoteEditorAction.OnTitleChange("Shopping"))
        dataSource.shouldReturnError = true

        viewModel.events.test {
            viewModel.onAction(NoteEditorAction.OnSaveClick)
            // UiText.StringResource is not a data class, so assert on the event type only.
            assertThat(awaitItem()).isInstanceOf(NoteEditorEvent.ShowSnackbar::class)
        }
        assertThat(viewModel.state.value.isSaving).isFalse()
        assertThat(dataSource.currentNotes).isEmpty()
    }
}
