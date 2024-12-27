package de.nielsfalk.desktop.filecleaner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.nielsfalk.desktop.filecleaner.FileCleanerEvent.AddFiles
import de.nielsfalk.desktop.filecleaner.FileCleanerEvent.RemoveFile
import java.awt.datatransfer.DataFlavor.javaFileListFlavor
import java.io.File

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun DragFolderScreen(
    navigate: (Any) -> Unit,
    state: FileCleanerState,
    onEvent: (FileCleanerEvent) -> Unit
) {
    var showTargetBorder by remember { mutableStateOf(false) }
    val dragAndDropTarget = remember<DragAndDropTarget> {
        object : DragAndDropTarget {

            override fun onStarted(event: DragAndDropEvent) {
                showTargetBorder = true
            }

            override fun onEnded(event: DragAndDropEvent) {
                showTargetBorder = false
            }

            override fun onDrop(event: DragAndDropEvent): Boolean {
                event.getFiles()?.let {
                    onEvent(AddFiles(it))
                }
                return true
            }
        }
    }
    Scaffold(
        modifier = Modifier.dragAndDropTarget(
            shouldStartDragAndDrop = { true },
            target = dragAndDropTarget
        ),
        topBar = {
            TopAppBar(
                title = { Text("Find Duplicate Files") },
            )
        }
    ) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray)
                    .then(
                        if (showTargetBorder)
                            Modifier.border(BorderStroke(3.dp, Color.Black))
                        else
                            Modifier
                    )
            ) {
                Text("Drop files and folders here", Modifier.align(Alignment.Center))
            }
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(items = state.files.toList(), itemContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = it.absolutePath)
                        IconButton(onClick = { onEvent(RemoveFile(it)) }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "select folders"
                            )
                        }
                    }
                })
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = SpaceBetween) {
                Button(
                    enabled = state.files.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary),
                    onClick = { onEvent(FileCleanerEvent.RemoveAllFiles) }) {
                    Text("Remove all files above")
                }
                Button(
                    enabled = state.files.isNotEmpty(),
                    onClick = { navigate(FindDuplicates(listOf())) }) {
                    Text("Search folders")
                }
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
@OptIn(ExperimentalComposeUiApi::class)
private fun DragAndDropEvent.getFiles(): List<File>? =
    awtTransferable.run {
        if (isDataFlavorSupported(javaFileListFlavor))
            getTransferData(javaFileListFlavor) as? List<File>
        else null
    }
