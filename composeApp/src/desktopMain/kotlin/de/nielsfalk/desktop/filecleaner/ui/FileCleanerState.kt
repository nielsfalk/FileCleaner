package de.nielsfalk.desktop.filecleaner.ui

import de.nielsfalk.desktop.filecleaner.domain.LoadingState
import de.nielsfalk.desktop.filecleaner.domain.ScanResult
import java.io.File

data class FileCleanerState(
    val directoriesToScan: Set<File> = setOf(),
    val loadingState: LoadingState? = null,
    val scanResult: ScanResult? = null
)
