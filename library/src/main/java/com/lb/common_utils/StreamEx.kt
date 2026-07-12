package com.lb.common_utils

import androidx.annotation.IntRange
import androidx.annotation.WorkerThread
import java.io.Closeable
import java.io.InputStream

fun Closeable?.closeSilently() {
    if (this != null) try {
        this.close()
    } catch (_: Exception) {
    }
}


/**
 * Skips [size] bytes in the stream, attempting multiple strategies if [InputStream.skip] fails.
 *
 * @param size Number of bytes to skip.
 * @return Number of bytes actually skipped.
 */
@IntRange(from = 0L)
@WorkerThread
fun InputStream.skipForcibly(size: Long): Long {
    if (size <= 0L)
        return 0L
    var bytesSkippedSoFar = 0L
    while (bytesSkippedSoFar < size) {
        val bytesSkipped = try {
            skip(size - bytesSkippedSoFar)
        } catch (_: Exception) {
            0L
        }
        if (bytesSkipped <= 0L) {
            if (read() == -1) return bytesSkippedSoFar
            bytesSkippedSoFar++
        } else {
            bytesSkippedSoFar += bytesSkipped
        }
    }
    return bytesSkippedSoFar
}

/**
 * Reads [bytesToRead] bytes from the stream into the provided [byteArray].
 * Unlike [InputStream.read], this continues reading until the buffer is full or EOF is reached.
 *
 * @param byteArray The buffer to read into.
 * @param bytesToRead Number of bytes to read (defaults to array size).
 */
@WorkerThread
fun InputStream.readBytesIntoByteArray(byteArray: ByteArray, bytesToRead: Int = byteArray.size) {
    var offset = 0
    while (true) {
        val read = this.read(byteArray, offset, bytesToRead - offset)
        if (read == -1)
            break
        offset += read
        if (offset >= bytesToRead)
            break
    }
}
