package com.example.notezapp.notes.presentation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.notezapp.notes.presentation.editor.NoteEditorRoot
import com.example.notezapp.notes.presentation.list.NoteListRoot

fun NavGraphBuilder.notesGraph(navController: NavController) {
    navigation<NotesGraphRoute>(startDestination = NoteListRoute) {
        composable<NoteListRoute> {
            NoteListRoot(
                onNavigateToEditor = { noteId ->
                    navController.navigate(NoteEditorRoute(noteId))
                }
            )
        }
        composable<NoteEditorRoute> {
            NoteEditorRoot(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
