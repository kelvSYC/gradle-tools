package com.kelvsyc.gradle.ant.tasks

import com.kelvsyc.gradle.ant.AntElement
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity

/**
 * Ant's `junitreport`: renders the JUnit XML results of [resultsDirectory] as an HTML report in [outputDirectory].
 * Needs `org.apache.ant:ant-junit` on the Ant classpath, which the plugin adds by default.
 */
@CacheableTask
abstract class AntJUnitReport : AbstractAntTask() {
    /** The directory holding the JUnit XML result files. */
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val resultsDirectory: DirectoryProperty

    /** Ant include patterns selecting the result files. */
    @get:Input
    abstract val includes: SetProperty<String>

    /** The report format, `frames` or `noframes`. */
    @get:Input
    abstract val format: Property<String>

    /** The directory receiving the report. */
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    init {
        includes.convention(listOf("TEST-*.xml"))
        format.convention("noframes")
    }

    override fun elements(): List<AntElement> {
        val report = element("junitreport", "todir" to temporaryDir)
        report.children.add(
            element(
                "fileset",
                "dir" to resultsDirectory,
                "includes" to includes.get().joinToString(",")
            )
        )
        report.children.add(element("report", "format" to format, "todir" to outputDirectory))
        return listOf(report)
    }
}
