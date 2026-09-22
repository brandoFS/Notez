package com.example.notezapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.notezapp.core.designsystem.theme.NotezTheme
import com.example.notezapp.notes.presentation.NotesGraphRoute
import com.example.notezapp.notes.presentation.notesGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            NotezTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = NotesGraphRoute
                ) {
                    notesGraph(navController)
                }
            }
        }
    }
}
