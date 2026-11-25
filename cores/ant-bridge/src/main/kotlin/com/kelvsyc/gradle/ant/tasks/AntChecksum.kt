package com.kelvsyc.gradle.ant.tasks

import com.kelvsyc.gradle.ant.AntElement
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import javax.inject.Inject

/**
 * Ant's `checksum`: writes a checksum file into [outputDirectory] for each of [files].
 */
@CacheableTask
abstract class AntChecksum @Inject constructor(objects: ObjectFactory) : AbstractAntTask(objects) {
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

    override fun elements(): List<AntElement> = files.files.map { file ->
        element(
            "checksum",
            "file" to file,
            "todir" to outputDirectory,
            "algorithm" to algorithm,
            "fileext" to fileExtension
        )
    }
}
