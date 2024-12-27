package de.nielsfalk.desktop.filecleaner.domain

import de.nielsfalk.desktop.filecleaner.domain.FileReader.listContainingFiles
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
                "sameContent2.txt",
                "sameContent1.txt",
                "sameContentInOtherDirectory.txt"
            ),
            result.map { it.fileName.toString() }
        )
    }
})

fun resourceAsPath(name: String): Path =
    Path.of(ClassLoader.getSystemResource(name).toURI())