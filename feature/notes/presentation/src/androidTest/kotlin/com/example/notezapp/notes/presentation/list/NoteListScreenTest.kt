package com.example.notezapp.notes.presentation.list

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.example.notezapp.core.presentation.UiText
import com.example.notezapp.notes.presentation.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class NoteListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val robot by lazy { NoteListRobot(composeTestRule) }

    private fun noteUi(id: String, title: String, preview: String = "Preview $id") = NoteUi(
        id = id,
        title = UiText.DynamicString(title),
        preview = preview,
        formattedDate = "Sep 22, 2026"
    )

    @Test
    fun displaysNotes_whenStateHasNotes() {
        robot
            .setContent(
                NoteListState(
                    isLoading = false,
                    notes = listOf(
                        noteUi("1", "Groceries", "Oat milk, lemons"),
                        noteUi("2", "Standup notes")
                    )
                )
            )
            .assertNoteVisible("Groceries")
            .assertPreviewVisible("Oat milk, lemons")
            .assertNoteVisible("Standup notes")
    }

    @Test
    fun showsEmptyState_whenThereAreNoNotes() {
        robot
            .setContent(NoteListState(isLoading = false))
            .assertEmptyState()
    }

    @Test
    fun clickingANote_emitsOnNoteClickWithItsId() {
        var action: NoteListAction? = null

        robot
            .setContent(
                state = NoteListState(isLoading = false, notes = listOf(noteUi("42", "Groceries"))),
                onAction = { action = it }
            )
            .clickNote("Groceries")

        assertEquals(NoteListAction.OnNoteClick("42"), action)
    }

    @Test
    fun searchField_isVisible_whenThereAreNotes() {
        robot
            .setContent(NoteListState(isLoading = false, notes = listOf(noteUi("1", "Groceries"))))
            .assertSearchFieldVisible()
    }

    @Test
    fun searchField_isHidden_whenThereAreNoNotesAtAll() {
        robot
            .setContent(NoteListState(isLoading = false))
            .assertEmptyState()
            .assertSearchFieldAbsent()
    }

    @Test
    fun typingInSearch_emitsOnSearchQueryChange() {
        // A single character keeps this deterministic: the screen is stateless, so its
        // `value` never advances and multi-char input could arrive in one commit or several.
        var query: String? = null

        robot
            .setContent(
                state = NoteListState(isLoading = false, notes = listOf(noteUi("1", "Groceries"))),
                onAction = { if (it is NoteListAction.OnSearchQueryChange) query = it.query }
            )
            .typeInSearch("g")

        assertEquals("g", query)
    }

    @Test
    fun showsNoResultsState_whenAQueryMatchesNothing() {
        robot
            .setContent(NoteListState(isLoading = false, notes = emptyList(), searchQuery = "zzz"))
            .assertNoResultsState()
            .assertSearchFieldVisible()
    }

    @Test
    fun swipingANoteAway_emitsDeleteExactlyOnce() {
        val deleted = mutableListOf<String>()

        robot
            .setContent(
                state = NoteListState(isLoading = false, notes = listOf(noteUi("1", "Groceries"))),
                onAction = { if (it is NoteListAction.OnDeleteNote) deleted += it.noteId }
            )
            .swipeNoteAway("1")

        composeTestRule.waitForIdle()

        assertEquals(listOf("1"), deleted)
    }

    @Test
    fun undoingADelete_restoresTheRowWithoutDeletingItAgain() {
        val note = noteUi("1", "Groceries")
        val deleted = mutableListOf<String>()
        val state = mutableStateOf(NoteListState(isLoading = false, notes = listOf(note)))

        robot
            .setContent(
                stateProvider = { state.value },
                onAction = { if (it is NoteListAction.OnDeleteNote) deleted += it.noteId }
            )
            .swipeNoteAway("1")

        // The removal has to land *after* the row has finished settling, the way it does in the
        // app: the delete round-trips through Room and comes back on a Flow. Dropping the note
        // synchronously inside onAction disposes the row while it is still mid-animation, which
        // is what made an earlier version of this test pass against the broken screen.
        composeTestRule.waitForIdle()
        composeTestRule.runOnIdle { state.value = state.value.copy(notes = emptyList()) }
        composeTestRule.waitForIdle()

        // Undo puts the note back under the same list key. LazyColumn hangs on to saveable state
        // per key even after the item is disposed, so the row must not come back still marked
        // dismissed — that would both hide it and settle it into a second delete.
        composeTestRule.runOnIdle { state.value = state.value.copy(notes = listOf(note)) }
        composeTestRule.waitForIdle()

        assertEquals(listOf("1"), deleted)
        robot.assertNoteVisible("Groceries")
    }

    @Test
    fun clickingTheFab_emitsOnAddNoteClick() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var action: NoteListAction? = null

        robot
            .setContent(
                state = NoteListState(isLoading = false),
                onAction = { action = it }
            )
            .clickAddNote(context.getString(R.string.cd_add_note))

        assertEquals(NoteListAction.OnAddNoteClick, action)
    }
}
