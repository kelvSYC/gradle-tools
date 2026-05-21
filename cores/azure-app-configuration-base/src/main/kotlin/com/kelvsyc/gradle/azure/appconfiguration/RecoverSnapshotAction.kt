package com.kelvsyc.gradle.azure.appconfiguration

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] implementation that recovers a snapshot in Azure App Configuration.
 *
 * Recovering a snapshot restores all of its configuration settings as the latest version
 * in the store, overwriting any current values with the snapshot's values.
 */
abstract class RecoverSnapshotAction : WorkAction<RecoverSnapshotAction.Parameters> {
    /**
     * Parameters for [RecoverSnapshotAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the App Configuration client. */
        @get:Internal
        val service: Property<AppConfigurationClientBuildService>

        /** The name of the snapshot to recover. */
        val snapshotName: Property<String>
    }

    override fun execute() {
        parameters.service.get().getClient()
            .recoverSnapshot(parameters.snapshotName.get())
    }
}
