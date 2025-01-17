package de.nielsfalk.desktop.filecleaner.domain

import de.nielsfalk.desktop.filecleaner.domain.FileReader.countInDirectories
import de.nielsfalk.desktop.filecleaner.domain.FileReader.deleteDuplicatesIn
import de.nielsfalk.desktop.filecleaner.domain.FileReader.duplicatesByHashedContentAndSize
import de.nielsfalk.desktop.filecleaner.domain.FileReader.duplicatesBySize
import de.nielsfalk.desktop.filecleaner.domain.FileReader.listContainingFiles
import de.nielsfalk.desktop.filecleaner.domain.FileReader.scanForDuplicates
import de.nielsfalk.desktop.filecleaner.domain.FileReader.toFileInfos
import de.nielsfalk.desktop.filecleaner.domain.LoadingState.AdjustScanResult
import de.nielsfalk.desktop.filecleaner.domain.LoadingState.DeletingFiles
import io.kotest.core.spec.style.FreeSpec
import io.kotest.core.spec.style.scopes.FreeSpecContainerScope
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FileReaderTest : FreeSpec({
    "list files" {
        val givenPaths = setOf(
            resourceAsPath("testfiles"),
            resourceAsPath("othertestfiles/sameContentInOtherDirectory.txt")
        )

        val result = givenPaths.listContainingFiles()

        assertEquals(
            listOf(
                "differentContent.txt",
                "sameSize.txt",
                "sameContent2.txt",
                "sameContent1.txt",
                "sameContentInOtherDirectory.txt"
            ),
            result.map { it.fileName.toString() }
        )
    }

    "read fileInfo" {
        val givenFile = resourceAsPath("othertestfiles/sameContentInOtherDirectory.txt")

        val result = FileInfo(givenFile)

        assertEquals(12L, result.size)
        assertEquals("pja9fNQgYKTQf6G_vMAQ63eUwrpyHh4-TCAzWhW2bq8=", result.hashedContent)
    }

    "duplicates by size" {
        val givenFileInfos = setOf(
            resourceAsPath("testfiles"),
            resourceAsPath("othertestfiles/sameContentInOtherDirectory.txt")
        )
            .listContainingFiles()
            .toFileInfos()

        val result: Map<Long, List<FileInfo>> = givenFileInfos
            .duplicatesBySize()

        assertEquals(
            mapOf(
                12L to listOf(
                    "sameSize.txt",
                    "sameContent2.txt",
                    "sameContent1.txt",
                    "sameContentInOtherDirectory.txt"
                )
            ),
            result.mapValues { (_, files) -> files.map { it.path.fileName.toString() } }
        )
    }

    "duplicates by size and hash" {
        val givenDuplicatesBySize = setOf(
            resourceAsPath("testfiles"),
            resourceAsPath("othertestfiles/sameContentInOtherDirectory.txt")
        )
            .listContainingFiles()
            .toFileInfos()
            .duplicatesBySize()

        val result: Map<String, List<FileInfo>> = givenDuplicatesBySize
            .duplicatesByHashedContentAndSize()


        assertEquals(
            mapOf(
                "12 pja9fNQgYKTQf6G_vMAQ63eUwrpyHh4-TCAzWhW2bq8=" to listOf(
                    "sameContent2.txt",
                    "sameContent1.txt",
                    "sameContentInOtherDirectory.txt"
                )
            ),
            result.mapValues { (_, files) -> files.map { it.path.fileName.toString() } }
        )
    }

    "directoryCounts" {
        val givenFileInfos = setOf(
            resourceAsPath("testfiles"),
            resourceAsPath("othertestfiles/sameContentInOtherDirectory.txt")
        )
            .listContainingFiles()
            .toFileInfos()
        val givenDuplicatesByHashedContent = givenFileInfos
            .duplicatesBySize()
            .duplicatesByHashedContentAndSize()

        val result = countInDirectories(givenFileInfos, givenDuplicatesByHashedContent)

        assertEquals(
            listOf(
                DirectoryCounts(
                    resourceAsPath("testfiles"),
                    duplicatesCount = 2,
                    nonDuplicatesCount = 2
                ),
                DirectoryCounts(
                    resourceAsPath("othertestfiles"),
                    duplicatesCount = 1,
                    nonDuplicatesCount = 0
                )
            ),
            result
        )
    }

    "scanForDuplicates" {
        val recordedStates = mutableListOf<LoadingState>()
        val givenPaths = setOf(
            resourceAsPath("testfiles"),
            resourceAsPath("othertestfiles/sameContentInOtherDirectory.txt")
        )

        givenPaths.scanForDuplicates({ recordedStates += it })

        assertEquals(
            LoadingState.entries - listOf(DeletingFiles, AdjustScanResult),
            recordedStates.toList()
        )
    }

    "deleteDuplicatesIn" - {
        data class TestData(
            val givenPaths: Set<Path>,
            val deleteDuplicatesIn: Path,
            val expectedDeletedFiles: Set<Path>,
            val expectedNewScanResultDirectoryCount: List<DirectoryCounts> = emptyList(),
            val expectedNewScanResultDuplicatesByHashedContentAndSize: Map<String, List<FileInfo>> = emptyMap(),
        )

        testOn(
            TestData(
                givenPaths = setOf(
                    resourceAsPath("testfiles"),
                    resourceAsPath("othertestfiles")
                ),
                deleteDuplicatesIn = resourceAsPath("testfiles"),
                expectedDeletedFiles = setOf(
                    resourceAsPath("testfiles/sameContent1.txt"),
                    resourceAsPath("testfiles/sameContent2.txt")
                )
            ),
            TestData(
                givenPaths = setOf(
                    resourceAsPath("testfiles"),
                ),
                deleteDuplicatesIn = resourceAsPath("testfiles"),
                expectedDeletedFiles = setOf(
                    resourceAsPath("testfiles/sameContent1.txt"),
                    //resourceAsPath("testfiles/sameContent2.txt")
                )
            ),
            TestData(
                givenPaths = setOf(
                    resourceAsPath("testfiles"),
                    resourceAsPath("othertestfiles")
                ),
                deleteDuplicatesIn = resourceAsPath("othertestfiles"),
                expectedDeletedFiles = setOf(
                    resourceAsPath("othertestfiles/sameContentInOtherDirectory.txt")
                ),
                expectedNewScanResultDuplicatesByHashedContentAndSize = mapOf(
                    "12 pja9fNQgYKTQf6G_vMAQ63eUwrpyHh4-TCAzWhW2bq8=" to listOf(
                        FileInfo(path = resourceAsPath("testfiles/sameContent2.txt"), size = 12),
                        FileInfo(path = resourceAsPath("testfiles/sameContent1.txt"), size = 12)
                    )
                ),
                expectedNewScanResultDirectoryCount = listOf(
                    DirectoryCounts(
                        directory = resourceAsPath("testfiles"),
                        duplicatesCount = 2,
                        nonDuplicatesCount = 2
                    )
                )
            )
        ) {
            val scanResult = givenPaths.scanForDuplicates()
            val recordedFilesToDelete = mutableSetOf<Path>()

            val newScanResult = deleteDuplicatesIn(
                directory = deleteDuplicatesIn,
                scanResult = scanResult,
                fileDeleter = { recordedFilesToDelete += it }
            )

            assertEquals(
                expectedDeletedFiles,
                recordedFilesToDelete
            )
            assertTrue(
                newScanResult.fileInfos.none { it.path in expectedDeletedFiles }
            )
            assertEquals(
                expectedNewScanResultDuplicatesByHashedContentAndSize,
                newScanResult.duplicatesByHashedContentAndSize
            )
            assertEquals(
                expectedNewScanResultDirectoryCount,
                newScanResult.countInDirectories
            )
        }

    }

})

suspend fun <T> FreeSpecContainerScope.testOn(
    vararg testData: T,
    function: suspend T.() -> Unit
) {
    testData.forEach {
        "$it" {
            it.function()
        }

    }
}

fun resourceAsPath(name: String): Path =
    Path.of(ClassLoader.getSystemResource(name).toURI())