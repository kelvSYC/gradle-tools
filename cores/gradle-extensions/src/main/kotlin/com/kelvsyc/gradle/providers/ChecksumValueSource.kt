package com.kelvsyc.gradle.providers

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import java.io.IOException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * Gradle [ValueSource] that computes a checksum of a file using a specified digest algorithm.
 *
 * The checksum is returned as a lowercase hex-encoded string. If the file cannot be read or the algorithm
 * is not available, no value will be provided.
 */
abstract class ChecksumValueSource : ValueSource<String, ChecksumValueSource.Parameters> {
    /**
     * Parameters for [ChecksumValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The input file to compute the checksum of.
         */
        val inputFile: RegularFileProperty

        /**
         * The digest algorithm to use (e.g. `SHA-256`, `SHA-512`, `MD5`).
         */
        val algorithm: Property<String>
    }

    @Suppress("MagicNumber")
    override fun obtain(): String? {
        return try {
            val digest = MessageDigest.getInstance(parameters.algorithm.get())
            val bytes = parameters.inputFile.get().asFile.readBytes()
            val hash = digest.digest(bytes)
            hash.joinToString("") { "%02x".format(it) }
        } catch (_: IOException) {
            null
        } catch (_: NoSuchAlgorithmException) {
            null
        }
    }
}
