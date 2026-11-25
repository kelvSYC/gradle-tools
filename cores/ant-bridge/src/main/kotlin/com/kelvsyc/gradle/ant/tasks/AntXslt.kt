package com.kelvsyc.gradle.ant.tasks

import com.kelvsyc.gradle.ant.AntElement
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity

/**
 * Ant's `xslt`: transforms the files of [sourceDirectory] with [stylesheet] into [outputDirectory].
 */
@CacheableTask
abstract class AntXslt : AbstractAntTask() {
    /** The directory whose files are transformed (Ant's `basedir`). */
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceDirectory: DirectoryProperty

    /** Ant include patterns; every file by default. */
    @get:Input
    abstract val includes: SetProperty<String>

    /** Ant exclude patterns. */
    @get:Input
    abstract val excludes: SetProperty<String>

    /** The XSLT stylesheet (Ant's `style`). */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val stylesheet: RegularFileProperty

    /** The stylesheet parameters, passed as nested `param` elements. */
    @get:Input
    abstract val parameters: MapProperty<String, String>

    /** The extension of the output files (Ant's `extension`). */
    @get:Input
    abstract val extension: Property<String>

    /** The directory receiving the output files (Ant's `destdir`). */
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    init {
        includes.convention(listOf("**/*"))
        excludes.convention(emptyList())
        extension.convention(".html")
    }

    /** Clears [outputDirectory], so that outputs of removed inputs do not survive. */
    override fun prepare() = clearDirectory(outputDirectory)

    override fun elements(): List<AntElement> {
        val xslt = element(
            "xslt",
            "basedir" to sourceDirectory,
            "destdir" to outputDirectory,
            "style" to stylesheet,
            "force" to true,
            "extension" to extension,
            "includes" to includes.get().joinToString(","),
            "excludes" to excludes.get().takeIf { it.isNotEmpty() }?.joinToString(",")
        )
        parameters.get().forEach { (name, value) ->
            xslt.children.add(element("param", "name" to name, "expression" to value))
        }
        return listOf(xslt)
    }
}
