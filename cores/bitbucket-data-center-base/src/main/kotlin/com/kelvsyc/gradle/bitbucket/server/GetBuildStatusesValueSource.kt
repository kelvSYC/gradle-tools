package com.kelvsyc.gradle.bitbucket.server

import com.kelvsyc.gradle.bitbucket.server.model.BuildStatus
import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters

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
        val service: Property<ClientsBaseService>

        /**
         * Registered name of a [BitbucketServerClientInfo].
         */
        val clientName: Property<String>

        /**
         * The full commit hash.
         */
        val commitId: Property<String>
    }

    private val client: Provider<BitbucketServerService> =
        parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun obtain(): List<BuildStatus> {
        val service = client.get()
        val commit = parameters.commitId.get()
        return fetchAllPages { start ->
            service.getBuildStatuses(commitId = commit, start = start)
        }
    }
}
