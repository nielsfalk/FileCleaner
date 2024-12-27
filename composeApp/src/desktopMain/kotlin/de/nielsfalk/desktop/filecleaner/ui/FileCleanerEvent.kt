package de.nielsfalk.desktop.filecleaner.ui

import java.io.File

sealed interface FileCleanerEvent {
    data class AddFiles(val files: List<File>) : FileCleanerEvent
    data class RemoveFile(val file: File) : FileCleanerEvent
    data object RemoveAllFiles: FileCleanerEvent
}