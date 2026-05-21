package com.kelvsyc.gradle.azure.appconfiguration

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] implementation that archives a snapshot in Azure App Configuration.
 *
 * An archived snapshot is retained indefinitely for audit and compliance purposes, but is
 * excluded from normal snapshot listings and recovery operations.
 */
abstract class ArchiveSnapshotAction : WorkAction<ArchiveSnapshotAction.Parameters> {
    /**
     * Parameters for [ArchiveSnapshotAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the App Configuration client. */
        @get:Internal
        val service: Property<AppConfigurationClientBuildService>

        /** The name of the snapshot to archive. */
        val snapshotName: Property<String>
    }

    override fun execute() {
        parameters.service.get().getClient()
            .archiveSnapshot(parameters.snapshotName.get())
    }
}
