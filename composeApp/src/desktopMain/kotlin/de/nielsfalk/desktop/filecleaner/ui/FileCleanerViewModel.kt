package de.nielsfalk.desktop.filecleaner.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.nielsfalk.desktop.filecleaner.domain.FileReader.deleteDuplicatesIn
import de.nielsfalk.desktop.filecleaner.domain.FileReader.scanForDuplicates
import de.nielsfalk.desktop.filecleaner.ui.FileCleanerEvent.AddFiles
import de.nielsfalk.desktop.filecleaner.ui.FileCleanerEvent.RemoveAllFiles
import de.nielsfalk.desktop.filecleaner.ui.FileCleanerEvent.RemoveFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FileCleanerViewModel : ViewModel() {
    private val _state = MutableStateFlow(FileCleanerState())
    val state: StateFlow<FileCleanerState> = _state.asStateFlow()

    fun onEvent(event: FileCleanerEvent) {
        when (event) {
            is AddFiles ->
                _state.update { it.copy(directoriesToScan = it.directoriesToScan + event.files) }

            is RemoveFile ->
                _state.update { it.copy(directoriesToScan = it.directoriesToScan - event.file) }

            is RemoveAllFiles ->
                _state.update { it.copy(directoriesToScan = setOf()) }

            FileCleanerEvent.ScanForDuplicates ->
                viewModelScope.launch {
                    val stateOnLaunch = state.value
                    val scanResult = stateOnLaunch.directoriesToScan
                        .map { file -> file.toPath() }
                        .toSet()
                        .scanForDuplicates(loadingStateConsumer = { newState ->
                            _state.update {
                                if (stateOnLaunch.directoriesToScan == it.directoriesToScan)
                                    it.copy(loadingState = newState)
                                else it
                            }
                        })
                    _state.update {
                        if (stateOnLaunch.directoriesToScan == it.directoriesToScan)
                            it.copy(scanResult = scanResult, loadingState = null)
                        else it
                    }
                }


            is FileCleanerEvent.DeleteDuplicatesIn ->
                viewModelScope.launch {
                    val stateOnLaunch = state.value
                    val newScanResult = deleteDuplicatesIn(
                        directory = event.directory,
                        scanResult = event.scanResult,
                        loadingStateConsumer = { newState ->
                            _state.update {
                                if (stateOnLaunch.directoriesToScan == it.directoriesToScan)
                                    it.copy(loadingState = newState)
                                else it
                            }
                        }
                    )
                    _state.update {
                        if (stateOnLaunch.directoriesToScan == it.directoriesToScan)
                            it.copy(loadingState = null, scanResult = newScanResult)
                        else it
                    }
                }
        }
    }
}