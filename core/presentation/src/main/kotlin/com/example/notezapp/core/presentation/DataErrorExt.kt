package com.example.notezapp.core.presentation

import com.example.notezapp.core.domain.DataError

fun DataError.toUiText(): UiText = when (this) {
    DataError.Local.DISK_FULL -> UiText.StringResource(R.string.error_disk_full)
    DataError.Local.NOT_FOUND -> UiText.StringResource(R.string.error_not_found)
    DataError.Local.UNKNOWN -> UiText.StringResource(R.string.error_unknown)
}
