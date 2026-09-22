package com.example.notezapp.notes.domain

data class Note(
    val id: String,
    val title: String,
    val content: String,
    val updatedAt: Long
)
