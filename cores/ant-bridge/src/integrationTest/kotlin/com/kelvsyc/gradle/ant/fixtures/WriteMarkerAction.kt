package com.kelvsyc.gradle.ant.fixtures

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * A task's own work item, submitted next to an Ant work item in the mixed-queue test: writes [Parameters.text] to
 * [Parameters.marker].
 */
abstract class WriteMarkerAction : WorkAction<WriteMarkerAction.Parameters> {
    /**
     * Parameters of [WriteMarkerAction].
     */
    interface Parameters : WorkParameters {
        /** The file to write. */
        val marker: RegularFileProperty

        /** The text to write. */
        val text: Property<String>
    }

    override fun execute() {
        val file = parameters.marker.get().asFile
        file.parentFile.mkdirs()
        file.writeText(parameters.text.get())
    }
}
