package com.example.notezapp

import android.app.Application
import com.example.notezapp.notes.data.di.notesDataModule
import com.example.notezapp.notes.presentation.di.notesPresentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class NotezApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@NotezApplication)
            modules(
                notesDataModule,
                notesPresentationModule
            )
        }
    }
}
