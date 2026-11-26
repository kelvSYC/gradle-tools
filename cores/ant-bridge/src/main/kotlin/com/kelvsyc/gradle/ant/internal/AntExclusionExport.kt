package com.kelvsyc.gradle.ant.internal

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * Writes the properties a replacement supplies for an excluded imported Ant target to [exportFile], so the targets
 * downstream of it load them as a declared input. [properties] carries the dependency on the replacement. Registered
 * by `antBridge.importBuild`; abstract only so that Gradle can generate its managed properties, and not designed to
 * be extended.
 */
@DisableCachingByDefault(because = "Writing a small properties file is cheaper than caching it")
abstract class AntExclusionExport : DefaultTask() {
    /** The supplied properties. */
    @get:Input
    abstract val properties: MapProperty<String, String>

    /** The properties file to write [properties] to. */
    @get:OutputFile
    abstract val exportFile: RegularFileProperty

    /** Writes [properties] to [exportFile]. */
    @TaskAction
    fun write() {
        PropertiesFiles.write(exportFile.get().asFile, properties.get())
    }
}
