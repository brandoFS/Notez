package com.example.notezapp.notes.presentation.list

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import com.example.notezapp.core.designsystem.theme.NotezTheme

class NoteListRobot(private val composeTestRule: ComposeContentTestRule) {

    fun setContent(
        state: NoteListState,
        onAction: (NoteListAction) -> Unit = {}
    ) = apply {
        composeTestRule.setContent {
            NotezTheme {
                NoteListScreen(state = state, onAction = onAction)
            }
        }
    }

    /**
     * For tests that need the list to change mid-test — deleting a note, undoing it — so the
     * screen re-reads [stateProvider] the way the real `collectAsStateWithLifecycle` would.
     */
    fun setContent(
        stateProvider: () -> NoteListState,
        onAction: (NoteListAction) -> Unit
    ) = apply {
        composeTestRule.setContent {
            NotezTheme {
                NoteListScreen(state = stateProvider(), onAction = onAction)
            }
        }
    }

    fun swipeNoteAway(noteId: String) = apply {
        composeTestRule.onNodeWithTag(noteRowTestTag(noteId)).performTouchInput { swipeLeft() }
    }

    fun assertNoteVisible(title: String) = apply {
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }

    fun assertPreviewVisible(preview: String) = apply {
        composeTestRule.onNodeWithText(preview).assertIsDisplayed()
    }

    fun assertEmptyState() = apply {
        composeTestRule.onNodeWithTag(EmptyNotesTestTag).assertIsDisplayed()
    }

    fun assertNoResultsState() = apply {
        composeTestRule.onNodeWithTag(NoResultsTestTag).assertIsDisplayed()
    }

    fun assertSearchFieldVisible() = apply {
        composeTestRule.onNodeWithTag(SearchFieldTestTag).assertIsDisplayed()
    }

    fun assertSearchFieldAbsent() = apply {
        composeTestRule.onNodeWithTag(SearchFieldTestTag).assertDoesNotExist()
    }

    fun typeInSearch(text: String) = apply {
        composeTestRule.onNodeWithTag(SearchFieldTestTag).performTextInput(text)
    }

    fun clickNote(title: String) = apply {
        composeTestRule.onNodeWithText(title).performClick()
    }

    fun clickAddNote(contentDescription: String) = apply {
        composeTestRule.onNodeWithContentDescription(contentDescription).performClick()
    }
}
