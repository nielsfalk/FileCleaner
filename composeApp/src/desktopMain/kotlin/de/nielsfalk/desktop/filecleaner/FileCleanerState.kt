package de.nielsfalk.desktop.filecleaner

import java.io.File

data class FileCleanerState(
    val files: Set<File> = setOf()
)
