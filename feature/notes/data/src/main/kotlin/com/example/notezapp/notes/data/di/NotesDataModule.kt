package com.example.notezapp.notes.data.di

import androidx.room.Room
import com.example.notezapp.notes.data.NotezDatabase
import com.example.notezapp.notes.data.RoomNoteDataSource
import com.example.notezapp.notes.domain.NoteLocalDataSource
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val notesDataModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            NotezDatabase::class.java,
            "notez.db"
        ).build()
    }
    single { get<NotezDatabase>().noteDao }
    singleOf(::RoomNoteDataSource) bind NoteLocalDataSource::class
}
