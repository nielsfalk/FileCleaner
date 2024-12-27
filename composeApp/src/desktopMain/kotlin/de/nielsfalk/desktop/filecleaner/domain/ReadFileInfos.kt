package de.nielsfalk.desktop.filecleaner.domain

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isDirectory

object FileReader {
    fun Set<Path>.listContainingFiles(): List<Path> =
        flatMap { it.listContainingFiles() }

    fun Path.listContainingFiles(): Set<Path> {
        return Files.walk(this)
            .filter { !it.isDirectory() }
            .toList()
            .toSet()
    }
}
