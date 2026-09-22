package com.example.notezapp.notes.presentation.list

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
