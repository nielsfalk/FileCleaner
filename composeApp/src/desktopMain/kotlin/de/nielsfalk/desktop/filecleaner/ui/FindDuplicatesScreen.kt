package de.nielsfalk.desktop.filecleaner.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.nielsfalk.desktop.filecleaner.ui.FileCleanerEvent.DeleteDuplicatesIn

@Composable
fun FindDuplicatesScreen(
    navigate: (Any) -> Unit,
    state: FileCleanerState,
    onEvent: (FileCleanerEvent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.loadingState != null) "Finding duplicates"
                        else "Review duplicates"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navigate(DragFolders) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "select folders"
                        )
                    }
                }
            )
        }
    ) {

        state.loadingState?.let {
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("$it")
                CircularProgressIndicator(
                    modifier = Modifier.width(64.dp)
                )
            }
        } ?: state.scanResult?.let { scanResult ->
            if (scanResult.countInDirectories.isEmpty())
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("no duplicates found")
                }
            else
                LazyColumn {
                    items(
                        items = scanResult.countInDirectories,
                        itemContent = {
                            Row {
                                Column {
                                    Text(it.directory.toString())
                                    Text("  ${it.duplicatesCount} duplicates")
                                    Text("  ${it.nonDuplicatesCount} unique files")
                                }
                                IconButton(onClick = { onEvent(DeleteDuplicatesIn(it.directory, scanResult)) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "delete duplicates"
                                    )
                                }
                            }
                        }
                    )
                }
        }
    }
}