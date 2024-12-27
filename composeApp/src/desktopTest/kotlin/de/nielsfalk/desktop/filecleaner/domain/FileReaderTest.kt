package de.nielsfalk.desktop.filecleaner.domain

import de.nielsfalk.desktop.filecleaner.domain.FileReader.duplicatesBySize
import de.nielsfalk.desktop.filecleaner.domain.FileReader.listContainingFiles
import de.nielsfalk.desktop.filecleaner.domain.FileReader.toFileInfos
import io.kotest.core.spec.style.FreeSpec
import java.nio.file.Path
import kotlin.test.assertEquals

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
})

fun resourceAsPath(name: String): Path =
    Path.of(ClassLoader.getSystemResource(name).toURI())