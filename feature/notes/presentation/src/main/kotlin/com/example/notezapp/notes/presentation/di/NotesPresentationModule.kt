package com.example.notezapp.notes.presentation.di

import com.example.notezapp.notes.presentation.editor.NoteEditorViewModel
import com.example.notezapp.notes.presentation.list.NoteListViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val notesPresentationModule = module {
    viewModelOf(::NoteListViewModel)
    viewModelOf(::NoteEditorViewModel)
}
