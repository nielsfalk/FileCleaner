package de.nielsfalk.desktop.filecleaner.ui

import java.io.File

data class FileCleanerState(
    val files: Set<File> = setOf()
)
