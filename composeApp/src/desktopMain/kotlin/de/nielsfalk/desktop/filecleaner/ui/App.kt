package de.nielsfalk.desktop.filecleaner.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
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
        val viewModel = viewModel { FileCleanerViewModel() }
        val state = viewModel.state.collectAsState().value
        NavHost(navController, startDestination = DragFolders) {
            composable<DragFolders> {
                DragFolderScreen(
                    navController::navigate,
                    state,
                    viewModel::onEvent
                )
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