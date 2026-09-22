package com.example.notezapp.notes.presentation.list

import com.example.notezapp.core.presentation.UiText
import com.example.notezapp.notes.domain.Note
import com.example.notezapp.notes.presentation.R
import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime

private const val PREVIEW_MAX_CHARS = 120

data class NoteUi(
    val id: String,
    val title: UiText,
    val preview: String,
    val formattedDate: String
)

private val NoteDateFormat = LocalDateTime.Format {
    monthName(MonthNames.ENGLISH_ABBREVIATED)
    char(' ')
    day()
    chars(", ")
    year()
}

fun Note.toNoteUi(): NoteUi = NoteUi(
    id = id,
    // Either the user's own text or a localized fallback, so this is UiText rather than String.
    title = if (title.isBlank()) {
        UiText.StringResource(R.string.untitled_note)
    } else {
        UiText.DynamicString(title)
    },
    preview = content.replace('\n', ' ').take(PREVIEW_MAX_CHARS),
    formattedDate = formatNoteDate(updatedAt)
)

private fun formatNoteDate(epochMillis: Long): String =
    Instant.fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .format(NoteDateFormat)
