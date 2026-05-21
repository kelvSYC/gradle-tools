package com.kelvsyc.gradle.azure.appconfiguration

import com.azure.data.appconfiguration.models.ConfigurationSnapshotStatus
import com.azure.data.appconfiguration.models.SnapshotSelector
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] that lists configuration snapshots from Azure App Configuration,
 * optionally filtered by name pattern.
 *
 * Only snapshots with [ConfigurationSnapshotStatus.READY] status are included in the result.
 * Snapshots in other states (e.g., ARCHIVED) are filtered out.
 *
 * Returns a [List] of snapshot names. Returns an empty list if no snapshots match the criteria.
 */
abstract class ListSnapshotsValueSource :
    ValueSource<List<String>, ListSnapshotsValueSource.Parameters> {

    /**
     * Parameters for [ListSnapshotsValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The build service managing the App Configuration client.
         */
        @get:Internal
        val service: Property<AppConfigurationClientBuildService>

        /**
         * Optional snapshot name pattern filter. When absent, all snapshots are included
         * (subject to the status filter).
         */
        val nameFilter: Property<String>
    }

    override fun obtain(): List<String> {
        val client = parameters.service.get().getClient()
        val selector = SnapshotSelector()
        if (parameters.nameFilter.isPresent) {
            selector.setNameFilter(parameters.nameFilter.get())
        }

        return client.listSnapshots(selector)
            .toList()
            .filter { it.status == ConfigurationSnapshotStatus.READY }
            .map { it.name }
    }
}


