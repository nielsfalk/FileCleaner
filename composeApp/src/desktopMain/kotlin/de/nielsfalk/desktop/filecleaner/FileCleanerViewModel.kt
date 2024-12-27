package de.nielsfalk.desktop.filecleaner

import androidx.lifecycle.ViewModel
import de.nielsfalk.desktop.filecleaner.FileCleanerEvent.AddFiles
import de.nielsfalk.desktop.filecleaner.FileCleanerEvent.RemoveAllFiles
import de.nielsfalk.desktop.filecleaner.FileCleanerEvent.RemoveFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FileCleanerViewModel : ViewModel() {
    private val _state = MutableStateFlow(FileCleanerState())
    val state: StateFlow<FileCleanerState> = _state.asStateFlow()

    fun onEvent(event: FileCleanerEvent) {
        when (event) {
            is AddFiles ->
                _state.update { it.copy(files = it.files + event.files) }

            is RemoveFile ->
                _state.update { it.copy(files = it.files - event.file) }

            is RemoveAllFiles ->
                _state.update { it.copy(files = setOf()) }
        }
    }
}