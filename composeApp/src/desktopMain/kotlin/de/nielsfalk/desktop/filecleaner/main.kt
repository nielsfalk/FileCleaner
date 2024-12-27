package de.nielsfalk.desktop.filecleaner

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import de.nielsfalk.desktop.filecleaner.ui.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "FileCleaner",
    ) {
        App()
    }
}