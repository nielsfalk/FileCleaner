package de.nielsfalk.desktop.filecleaner.domain


import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.util.*


fun Path.shaFirstKb(): String {
    val buffer: ByteBuffer = ByteBuffer.allocate(1024)
    return FileChannel.open(this).use {
        it.read(buffer)
        buffer.array().sha256()
    }
}

fun ByteArray.sha256(): String {
    val bytes = this
    return sha256 { update(bytes) }
}

fun ByteArray.urlEncoded(): String =
    Base64.getUrlEncoder().encodeToString(this)

fun Path.sha256(): String =
    Files.newInputStream(this).use {
        val buffer = ByteArray(1024 * 1024)
        sha256 {
            var bytesRead: Int

            while (it.read(buffer).also { bytesRead = it } != -1) {
                update(buffer, 0, bytesRead)
            }
        }
    }

fun sha256(function: MessageDigest.() -> Unit) =
    MessageDigest.getInstance("SHA-256")
        .also(function)
        .digest()
        .urlEncoded()