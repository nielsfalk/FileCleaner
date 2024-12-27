package de.nielsfalk.desktop.filecleaner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.awt.datatransfer.DataFlavor

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        val navController = rememberNavController()
        NavHost(navController, startDestination = DragFolders) {
            composable<DragFolders> {
                var showTargetBorder by remember { mutableStateOf(false) }
                var targetText by remember { mutableStateOf("Drop files and folders here") }
                val coroutineScope = rememberCoroutineScope()
                val dragAndDropTarget = remember {
                    object : DragAndDropTarget {

                        override fun onStarted(event: DragAndDropEvent) {
                            showTargetBorder = true
                        }

                        override fun onEnded(event: DragAndDropEvent) {
                            showTargetBorder = false
                        }

                        override fun onDrop(event: DragAndDropEvent): Boolean {
                            val result = targetText == "Drop files and folders here"
                            targetText = event.awtTransferable.let {
                                if (it.isDataFlavorSupported(DataFlavor.stringFlavor))
                                    it.getTransferData(DataFlavor.stringFlavor) as String
                                else
                                    it.transferDataFlavors.first().humanPresentableName
                            }
                            coroutineScope.launch {
                                delay(2000)
                                targetText = "Drop files and folders here"
                            }
                            return result
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
                            Text(targetText, Modifier.align(Alignment.Center))
                        }
                        Button(onClick = { navController.navigate(FindDuplicates(listOf())) }) {
                            Text("Search folders")
                        }
                    }
                }
            }
            composable<FindDuplicates> {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Finding duplicates") },
                            navigationIcon = {
                                IconButton(onClick = { navController.navigate(DragFolders) }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "select folders"
                                    )
                                }
                            }
                        )
                    }
                ) {

                }
            }
        }

    }
}

@Serializable
data object DragFolders

@Serializable
data class FindDuplicates(val folders: List<String>)