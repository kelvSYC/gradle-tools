package com.kelvsyc.gradle.bitbucket.server

import com.kelvsyc.gradle.bitbucket.server.model.BuildStatus
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] that fetches all build statuses for a commit from the Bitbucket Data Center API.
 *
 * Automatically follows pagination to collect statuses from all pages.
 */
abstract class GetBuildStatusesValueSource :
    ValueSource<List<BuildStatus>, GetBuildStatusesValueSource.Parameters> {
    /**
     * Parameters for [GetBuildStatusesValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The shared build service managing Bitbucket Data Center clients.
         */
        @get:Internal
        val service: Property<BitbucketServerClientBuildService>

        /**
         * The full commit hash.
         */
        val commitId: Property<String>
    }

    override fun obtain(): List<BuildStatus> {
        val bbService = parameters.service.get().getClient()
        val commit = parameters.commitId.get()
        return fetchAllPages { start ->
            bbService.getBuildStatuses(commitId = commit, start = start)
        }
    }
}
