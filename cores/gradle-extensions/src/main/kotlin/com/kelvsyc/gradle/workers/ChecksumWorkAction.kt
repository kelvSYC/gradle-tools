package com.kelvsyc.gradle.workers

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import java.security.MessageDigest

/**
 * [WorkAction] that computes the checksum of an input file and writes it to a sidecar file in the output directory.
 *
 * The output file contains the lowercase hex-encoded checksum followed by a newline.
 */
abstract class ChecksumWorkAction : WorkAction<ChecksumWorkAction.Parameters> {
    /**
     * Parameters for [ChecksumWorkAction].
     */
    interface Parameters : WorkParameters {
        /**
         * The input file to compute the checksum of.
         */
        val inputFile: RegularFileProperty

        /**
         * The digest algorithm to use (e.g. `SHA-256`, `SHA-512`, `MD5`).
         */
        val algorithm: Property<String>

        /**
         * The output file where the checksum will be written.
         */
        val outputFile: RegularFileProperty
    }

    @Suppress("MagicNumber")
    override fun execute() {
        val algorithm = parameters.algorithm.get()
        val digest = MessageDigest.getInstance(algorithm)
        val inputFile = parameters.inputFile.get().asFile
        val hash = digest.digest(inputFile.readBytes())
        val hex = hash.joinToString("") { "%02x".format(it) }

        parameters.outputFile.get().asFile.writeText("$hex\n")
    }
}
