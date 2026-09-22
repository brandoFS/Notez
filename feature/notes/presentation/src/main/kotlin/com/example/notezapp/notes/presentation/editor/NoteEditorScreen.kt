package com.example.notezapp.notes.presentation.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notezapp.core.designsystem.theme.NotezTheme
import com.example.notezapp.core.presentation.ObserveAsEvents
import com.example.notezapp.notes.presentation.R
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun NoteEditorRoot(
    onNavigateBack: () -> Unit,
    viewModel: NoteEditorViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            NoteEditorEvent.NavigateBack -> onNavigateBack()

            is NoteEditorEvent.ShowSnackbar -> scope.launch {
                snackbarHostState.showSnackbar(event.message.asString(context))
            }
        }
    }

    NoteEditorScreen(
        state = state,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    state: NoteEditorState,
    onAction: (NoteEditorAction) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            if (state.isNewNote) R.string.new_note else R.string.edit_note
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onAction(NoteEditorAction.OnBackClick) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_navigate_back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onAction(NoteEditorAction.OnSaveClick) },
                        enabled = !state.isSaving
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.cd_save_note)
                        )
                    }
                }
            )
        },
        // The keyboard is nearly always up on this screen, so the snackbar has to clear it.
        snackbarHost = {
            SnackbarHost(snackbarHostState, modifier = Modifier.imePadding())
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TextField(
                value = state.title,
                onValueChange = { onAction(NoteEditorAction.OnTitleChange(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.note_title_hint)) },
                textStyle = MaterialTheme.typography.titleLarge,
                singleLine = true,
                colors = transparentTextFieldColors()
            )
            TextField(
                value = state.content,
                onValueChange = { onAction(NoteEditorAction.OnContentChange(it)) },
                modifier = Modifier.fillMaxSize(),
                placeholder = { Text(stringResource(R.string.note_content_hint)) },
                textStyle = MaterialTheme.typography.bodyLarge,
                colors = transparentTextFieldColors()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun transparentTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent
)

@Preview
@Composable
private fun NoteEditorScreenPreview() {
    NotezTheme {
        NoteEditorScreen(
            state = NoteEditorState(
                title = "Standup notes",
                content = "Ship the Room migration, then pick up the nav refactor.",
                isNewNote = false
            ),
            onAction = {}
        )
    }
}

@Preview
@Composable
private fun NoteEditorScreenEmptyPreview() {
    NotezTheme {
        NoteEditorScreen(state = NoteEditorState(), onAction = {})
    }
}
