package de.nielsfalk.desktop.filecleaner.domain

import de.nielsfalk.desktop.filecleaner.domain.LoadingState.AdjustScanResult
import de.nielsfalk.desktop.filecleaner.domain.LoadingState.DeletingFiles
import de.nielsfalk.desktop.filecleaner.domain.LoadingState.FindDuplicatesInHashedContent
import de.nielsfalk.desktop.filecleaner.domain.LoadingState.FindDuplicatesInSize
import de.nielsfalk.desktop.filecleaner.domain.LoadingState.GroupByDirectory
import de.nielsfalk.desktop.filecleaner.domain.LoadingState.ListFiles
import de.nielsfalk.desktop.filecleaner.domain.LoadingState.ReadingSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.fileSize
import kotlin.io.path.isDirectory

object FileReader {
    suspend fun Set<Path>.scanForDuplicates(
        loadingStateConsumer: (LoadingState) -> Unit ={}
    ): ScanResult =
        withContext(Dispatchers.IO) {
            loadingStateConsumer(ListFiles)
            val listResult = listContainingFiles()
            loadingStateConsumer(ReadingSize)
            val fileInfos = listResult.toFileInfos()
            loadingStateConsumer(FindDuplicatesInSize)
            val duplicatesBySize = fileInfos.duplicatesBySize()
            loadingStateConsumer(FindDuplicatesInHashedContent)
            val duplicatesByHashedContentAndSize = duplicatesBySize.duplicatesByHashedContentAndSize()
            loadingStateConsumer(GroupByDirectory)
            val countInDirectories = countInDirectories(fileInfos, duplicatesByHashedContentAndSize)
            ScanResult(
                fileInfos = fileInfos,
                duplicatesByHashedContentAndSize = duplicatesByHashedContentAndSize,
                countInDirectories = countInDirectories
            )
        }

    suspend fun deleteDuplicatesIn(
        directory: Path,
        scanResult: ScanResult,
        loadingStateConsumer: (LoadingState) -> Unit ={},
        fileDeleter: (Set<Path>) -> Unit = { it.forEach(Path::deleteIfExists) },
    ): ScanResult =
        withContext(Dispatchers.IO) {
            loadingStateConsumer(DeletingFiles)

            val filesToDelete: Set<Path> = scanResult.duplicatesByHashedContentAndSize.values.filter { duplicateFiles ->
                duplicateFiles.any { it.path.parent == directory }
            }
                .flatMap { duplicateFileInfos ->
                    val duplicateFiles = duplicateFileInfos.map { it.path }
                    val duplicateInAnotherFolder = duplicateFiles.any { it.parent != directory }
                    if (duplicateInAnotherFolder) {
                        duplicateFiles.filter { it.parent == directory }
                    } else {
                        val survivingFile = duplicateFiles.maxBy { it.fileName.toString().length }
                        duplicateFiles.filter { it.parent == directory && it != survivingFile }
                    }
                }
                .toSet()


            fileDeleter(filesToDelete)

            loadingStateConsumer(AdjustScanResult)
            val fileInfos = scanResult.fileInfos.filter { it.path !in filesToDelete }
            val duplicatesByHashedContentAndSize = scanResult.duplicatesByHashedContentAndSize
                .map { (duplicationKey, files) ->
                    duplicationKey to files.filter {
                        it.path !in filesToDelete
                    }
                }
                .filter { (_, files) -> files.size > 1 }
                .toMap()


            val countInDirectories = countInDirectories(fileInfos, duplicatesByHashedContentAndSize)

            ScanResult(
                fileInfos = fileInfos,
                duplicatesByHashedContentAndSize = duplicatesByHashedContentAndSize,
                countInDirectories = countInDirectories
            )
        }

    fun Set<Path>.listContainingFiles(): List<Path> =
        flatMap { it.listContainingFiles() }

    internal fun Path.listContainingFiles(): Set<Path> {
        return Files.walk(this)
            .filter { !it.isDirectory() }
            .toList()
            .toSet()
    }

    internal fun List<Path>.toFileInfos() = map { FileInfo(it) }

    internal fun List<FileInfo>.duplicatesBySize(): Map<Long, List<FileInfo>> =
        groupBy { it.size }
            .filter { (_, files) -> files.size > 1 }

    internal fun Map<Long, List<FileInfo>>.duplicatesByHashedContentAndSize(): Map<String, List<FileInfo>> =
        flatMap { (size, files) ->
            files.groupBy { it.hashedContent }
                .filter { (_, files) -> files.size > 1 }
                .map { (hash, files) ->
                    "$size $hash" to files
                }

        }.toMap()

    internal fun countInDirectories(
        fileInfos: List<FileInfo>,
        groupedDuplicates: Map<*, List<FileInfo>>
    ): List<DirectoryCounts> {
        val duplicates: List<FileInfo> = groupedDuplicates.values.flatten()
        return duplicates.map { it.path.parent }.toSet().map { directory ->
            val duplicatesCount = duplicates.countForParent(directory)
            DirectoryCounts(
                directory = directory,
                duplicatesCount = duplicatesCount,
                nonDuplicatesCount = fileInfos.countForParent(directory) - duplicatesCount
            )
        }
    }


    private fun List<FileInfo>.countForParent(directory: Path): Int = count { it.path.parent == directory }
}


enum class LoadingState {
    ListFiles,
    ReadingSize,
    FindDuplicatesInSize,
    FindDuplicatesInHashedContent,
    GroupByDirectory,
    DeletingFiles,
    AdjustScanResult
}

data class FileInfo(
    val path: Path,
    val size: Long = path.fileSize(),
) {
    val hashedContent: String by lazy {
        path.sha256()
    }
}

data class DirectoryCounts(
    val directory: Path,
    val duplicatesCount: Int,
    val nonDuplicatesCount: Int
)

data class ScanResult(
    val fileInfos: List<FileInfo>,
    val duplicatesByHashedContentAndSize: Map<String, List<FileInfo>>,
    val countInDirectories: List<DirectoryCounts>
)
