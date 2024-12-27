package de.nielsfalk.desktop.filecleaner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.DataFlavor.javaFileListFlavor
import java.io.File

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun DragFolderScreen(navController: NavHostController) {
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
                val files = event.getFiles()
                println("files = ${files}")
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
            Button(onClick = { navController.navigate(FindDuplicates(listOf())) }) {
                Text("Search folders")
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
