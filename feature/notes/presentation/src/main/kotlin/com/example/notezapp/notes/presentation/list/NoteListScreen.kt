package com.example.notezapp.notes.presentation.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notezapp.core.designsystem.theme.NotezTheme
import com.example.notezapp.core.presentation.ObserveAsEvents
import com.example.notezapp.core.presentation.UiText
import com.example.notezapp.notes.presentation.R
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

const val EmptyNotesTestTag = "empty_notes"
const val NoResultsTestTag = "no_results"
const val SearchFieldTestTag = "search_field"

fun noteRowTestTag(noteId: String) = "note_row_$noteId"

@Composable
fun NoteListRoot(
    onNavigateToEditor: (String?) -> Unit,
    viewModel: NoteListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    // Snackbars are launched separately so a visible one never stalls event collection.
    val scope = rememberCoroutineScope()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is NoteListEvent.NavigateToEditor -> onNavigateToEditor(event.noteId)

            is NoteListEvent.ShowSnackbar -> scope.launch {
                snackbarHostState.showSnackbar(event.message.asString(context))
            }

            is NoteListEvent.ShowUndoSnackbar -> scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = event.message.asString(context),
                    actionLabel = context.getString(R.string.undo),
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.onAction(NoteListAction.OnUndoDelete)
                }
            }
        }
    }

    NoteListScreen(
        state = state,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    state: NoteListState,
    onAction: (NoteListAction) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = stringResource(R.string.notes)) })
        },
        // imePadding keeps the undo snackbar above the keyboard — without it, deleting
        // while the search field has focus hides the snackbar and undo is unreachable.
        snackbarHost = {
            SnackbarHost(snackbarHostState, modifier = Modifier.imePadding())
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAction(NoteListAction.OnAddNoteClick) }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.cd_add_note)
                )
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // A search box above "No notes yet" is just noise, so hide it until there is
            // something to search — or until a query has already narrowed things to zero.
            val hasSomethingToSearch = state.notes.isNotEmpty() || state.searchQuery.isNotBlank()
            if (!state.isLoading && hasSomethingToSearch) {
                SearchField(
                    query = state.searchQuery,
                    onQueryChange = { onAction(NoteListAction.OnSearchQueryChange(it)) },
                    onClear = { onAction(NoteListAction.OnClearSearch) }
                )
            }

            when {
                state.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                state.notes.isEmpty() && state.searchQuery.isBlank() ->
                    EmptyNotes(modifier = Modifier.fillMaxSize())

                state.notes.isEmpty() ->
                    NoSearchResults(query = state.searchQuery, modifier = Modifier.fillMaxSize())

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(items = state.notes, key = { it.id }) { note ->
                        SwipeableNoteRow(
                            note = note,
                            onClick = { onAction(NoteListAction.OnNoteClick(note.id)) },
                            onDelete = { onAction(NoteListAction.OnDeleteNote(note.id)) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag(SearchFieldTestTag),
        placeholder = { Text(text = stringResource(R.string.search_notes)) },
        leadingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = null)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.cd_clear_search)
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        )
    )
}

@Composable
private fun NoSearchResults(query: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.testTag(NoResultsTestTag).padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.no_results_title),
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = stringResource(R.string.no_results_subtitle, query),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableNoteRow(
    note: NoteUi,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val positionalThreshold = SwipeToDismissBoxDefaults.positionalThreshold
    // Deliberately `remember`, not `rememberSwipeToDismissBoxState` — that one saves through
    // LazyColumn's per-key saveable holder, which outlives the item. A note brought back by
    // undo reappears under the same key and would restore as already-dismissed: drawn off
    // screen, and settled in a dismissed direction, which fires the delete again. A partial
    // swipe isn't worth persisting, so a fresh state per appearance is the right trade.
    val dismissState = remember {
        SwipeToDismissBoxState(SwipeToDismissBoxValue.Settled, positionalThreshold)
    }

    SwipeToDismissBox(
        state = dismissState,
        // Fires once, when the row settles as dismissed. The `confirmValueChange` this
        // replaced is a predicate the gesture layer calls speculatively — several times per
        // swipe — so deleting from it deleted the note more than once.
        onDismiss = { onDelete() },
        enableDismissFromStartToEnd = false,
        backgroundContent = { DeleteBackground() },
        modifier = Modifier.testTag(noteRowTestTag(note.id))
    ) {
        NoteRow(note = note, onClick = onClick)
    }
}

@Composable
private fun DeleteBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = stringResource(R.string.cd_delete_note),
            tint = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun NoteRow(
    note: NoteUi,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            // Opaque so the swipe-to-delete background stays hidden until the row moves.
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = note.title.asString(),
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (note.preview.isNotBlank()) {
            Text(
                text = note.preview,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = note.formattedDate,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyNotes(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.testTag(EmptyNotesTestTag).padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.empty_notes_title),
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = stringResource(R.string.empty_notes_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview
@Composable
private fun NoteListScreenPreview() {
    NotezTheme {
        NoteListScreen(
            state = NoteListState(
                isLoading = false,
                notes = listOf(
                    NoteUi(
                        id = "1",
                        title = UiText.DynamicString("Groceries"),
                        preview = "Oat milk, sourdough, chilli oil, lemons",
                        formattedDate = "Sep 22, 2026"
                    ),
                    NoteUi(
                        id = "2",
                        title = UiText.DynamicString("Standup notes"),
                        preview = "Ship the Room migration, then pick up the nav refactor",
                        formattedDate = "Sep 21, 2026"
                    )
                )
            ),
            onAction = {}
        )
    }
}

@Preview
@Composable
private fun NoteListScreenEmptyPreview() {
    NotezTheme {
        NoteListScreen(state = NoteListState(isLoading = false), onAction = {})
    }
}

@Preview
@Composable
private fun NoteListScreenNoResultsPreview() {
    NotezTheme {
        NoteListScreen(
            state = NoteListState(isLoading = false, searchQuery = "quarterly"),
            onAction = {}
        )
    }
}
