package de.nielsfalk.desktop.filecleaner.domain

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.fileSize
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

    fun List<Path>.toFileInfos() = map { FileInfo(it) }

    fun List<FileInfo>.duplicatesBySize(): Map<Long, List<FileInfo>> =
        groupBy { it.size }
            .filter { (_, files) -> files.size > 1 }

    fun Map<Long, List<FileInfo>>.duplicatesByHashedContentAndSize(): Map<String, List<FileInfo>> {
        return flatMap { (size, files) ->
            files.groupBy { it.hashedContent }
                .filter { (_, files) -> files.size > 1 }
                .map { (hash, files) ->
                    "$size $hash" to files
                }

        }.toMap()
    }
}

data class FileInfo(
    val path: Path,
    val size: Long = path.fileSize(),
) {
    val hashedContent: String by lazy {
        path.sha256()
    }
}