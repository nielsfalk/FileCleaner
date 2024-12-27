package de.nielsfalk.desktop.filecleaner

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        val navController = rememberNavController()
        NavHost(navController, startDestination = DragFolders) {
            composable<DragFolders> {
                DragFolderScreen(navController)
            }
            composable<FindDuplicates> {
                FindDuplicatesScreen(navController)
            }
        }

    }
}

@Serializable
data object DragFolders

@Serializable
data class FindDuplicates(val folders: List<String>)