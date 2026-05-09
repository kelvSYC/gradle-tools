package com.kelvsyc.gradle.workers

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * [WorkAction] that creates a ZIP archive from a set of input files.
 *
 * This is intended as a migration aid for legacy tasks that create archives inline (e.g. via
 * `ant.zip`), allowing archive creation to participate in the Worker API without requiring
 * decomposition into a standalone [org.gradle.api.tasks.bundling.Zip] task. New tasks that
 * solely produce an archive should use the built-in `Zip` task type instead.
 */
abstract class ZipAction : WorkAction<ZipAction.Parameters> {
    /**
     * Parameters for [ZipAction].
     */
    interface Parameters : WorkParameters {
        /**
         * The base directory from which relative entry paths are computed. Each file in
         * [sourceFiles] that resides under this directory will be stored with a ZIP entry
         * path equal to its path relative to the base directory. Files outside the base
         * directory are stored with their file name only.
         */
        val baseDirectory: DirectoryProperty

        /**
         * The files to include in the archive.
         */
        val sourceFiles: ConfigurableFileCollection

        /**
         * The output ZIP file.
         */
        val outputFile: RegularFileProperty

        /**
         * The ZIP compression level (0–9). Defaults to the JDK default if unset.
         */
        val compressionLevel: Property<Int>
    }

    override fun execute() {
        val output = parameters.outputFile.get().asFile
        output.parentFile.mkdirs()

        val baseDir = parameters.baseDirectory.get().asFile.toPath()

        ZipOutputStream(output.outputStream().buffered()).use { zos ->
            if (parameters.compressionLevel.isPresent) {
                zos.setLevel(parameters.compressionLevel.get())
            }
            for (file in parameters.sourceFiles) {
                if (!file.isFile) continue
                val filePath = file.toPath()
                val entryName = if (filePath.startsWith(baseDir)) {
                    baseDir.relativize(filePath).toString()
                } else {
                    file.name
                }
                zos.putNextEntry(ZipEntry(entryName))
                file.inputStream().buffered().use { it.copyTo(zos) }
                zos.closeEntry()
            }
        }
    }
}
