package com.kelvsyc.gradle.azure.appconfiguration

import com.azure.core.util.Context
import com.azure.data.appconfiguration.models.ConfigurationSettingsFilter
import com.azure.data.appconfiguration.models.ConfigurationSnapshot
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import java.time.Duration

/**
 * [WorkAction] implementation that creates a snapshot in Azure App Configuration.
 *
 * A snapshot captures a consistent view of configuration settings at a point in time, filtered
 * by key and optional label. The snapshot is retained for the specified duration (if provided).
 * This operation blocks until snapshot creation completes.
 */
abstract class CreateSnapshotAction : WorkAction<CreateSnapshotAction.Parameters> {
    /**
     * Parameters for [CreateSnapshotAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the App Configuration client. */
        @get:Internal
        val service: Property<AppConfigurationClientBuildService>

        /** The name of the snapshot to create. */
        val snapshotName: Property<String>

        /** The key filter pattern for settings to include in the snapshot (e.g., "app.*"). */
        val keyFilter: Property<String>

        /** Optional label filter to include only settings with a matching label. */
        val labelFilter: Property<String>

        /** Optional retention period in seconds. If specified, the snapshot expires after this duration. */
        val retentionPeriod: Property<Long>
    }

    override fun execute() {
        val filter = ConfigurationSettingsFilter(parameters.keyFilter.get())
        if (parameters.labelFilter.isPresent) {
            filter.setLabel(parameters.labelFilter.get())
        }

        val snapshot = ConfigurationSnapshot(listOf(filter))
        if (parameters.retentionPeriod.isPresent) {
            snapshot.setRetentionPeriod(Duration.ofSeconds(parameters.retentionPeriod.get()))
        }

        parameters.service.get().getClient()
            .beginCreateSnapshot(parameters.snapshotName.get(), snapshot, Context.NONE)
            .waitForCompletion()
    }
}
