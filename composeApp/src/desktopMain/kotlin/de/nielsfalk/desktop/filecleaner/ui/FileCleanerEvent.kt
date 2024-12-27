package de.nielsfalk.desktop.filecleaner.ui

import de.nielsfalk.desktop.filecleaner.domain.ScanResult
import java.io.File
import java.nio.file.Path

sealed interface FileCleanerEvent {
    data class AddFiles(val files: List<File>) : FileCleanerEvent
    data class RemoveFile(val file: File) : FileCleanerEvent
    data object RemoveAllFiles: FileCleanerEvent
    data object ScanForDuplicates:FileCleanerEvent
    data class DeleteDuplicatesIn(val directory: Path, val scanResult: ScanResult):FileCleanerEvent
}