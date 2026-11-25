package com.kelvsyc.gradle.ant.tasks

import com.kelvsyc.gradle.ant.AntScope
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity

/**
 * Ant's `checksum`: writes a checksum file into [outputDirectory] for each of [files].
 */
@CacheableTask
abstract class AntChecksum : AbstractAntTask() {
    /** The files to checksum; they may come from different directories. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val files: ConfigurableFileCollection

    /** The digest algorithm (Ant's `algorithm`). */
    @get:Input
    abstract val algorithm: Property<String>

    /** The extension of the checksum files (Ant's `fileext`). */
    @get:Input
    abstract val fileExtension: Property<String>

    /** The directory receiving the checksum files (Ant's `todir`). */
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    init {
        algorithm.convention("SHA-256")
        fileExtension.convention(".sha256")
    }

    /**
     * Fails if two of [files] share a name, as their checksum files would clash, then clears [outputDirectory], so
     * that outputs of removed inputs do not survive.
     */
    override fun prepare() {
        requireDistinctNames(files.files, "files to checksum")
        clearDirectory(outputDirectory)
    }

    override fun AntScope.elements() {
        files.files.forEach { file ->
            task(
                "checksum",
                "file" to file,
                "todir" to outputDirectory,
                "algorithm" to algorithm,
                "fileext" to fileExtension,
                "forceoverwrite" to true
            )
        }
    }
}
