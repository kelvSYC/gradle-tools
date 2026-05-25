package com.kelvsyc.gradle.azure.containerapp.sources

import com.kelvsyc.gradle.azure.containerapp.ContainerAppBuildService
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] that retrieves the list of revision names for a container app.
 *
 * Returns a [List] of revision names (e.g., `["my-app--aaa111", "my-app--bbb222"]`),
 * or an empty list if no revisions are found.
 */
abstract class ListRevisionsValueSource :
    ValueSource<List<String>, ListRevisionsValueSource.Parameters> {

    /**
     * Parameters for [ListRevisionsValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The build service managing the container app.
         */
        @get:Internal
        val service: Property<ContainerAppBuildService>
    }

    override fun obtain(): List<String> {
        val appSvc = parameters.service.get()
        val envSvc = appSvc.parameters.environmentService.get()
        val rg = envSvc.parameters.resourceGroupName.get()
        val appName = appSvc.parameters.containerAppName.get()
        return envSvc.getClient().containerAppsRevisions().listRevisions(rg, appName)
            .map { it.name() }
    }
}
