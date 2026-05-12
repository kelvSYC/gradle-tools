package com.kelvsyc.gradle.bitbucket.server

import com.kelvsyc.gradle.bitbucket.server.model.Repository
import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] that fetches repository metadata from the Bitbucket Data Center API.
 */
abstract class GetRepositoryValueSource :
    ValueSource<Repository, GetRepositoryValueSource.Parameters> {
    /**
     * Parameters for [GetRepositoryValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The shared build service managing Bitbucket Data Center clients.
         */
        @get:Internal
        val service: Property<ClientsBaseService>

        /**
         * Registered name of a [BitbucketServerClientInfo].
         */
        val clientName: Property<String>

        /**
         * The Bitbucket project key.
         */
        val projectKey: Property<String>

        /**
         * The repository slug.
         */
        val repoSlug: Property<String>
    }

    private val client: Provider<BitbucketServerService> =
        parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun obtain(): Repository? {
        val response = client.get().getRepository(
            projectKey = parameters.projectKey.get(),
            repoSlug = parameters.repoSlug.get(),
        ).execute()

        return response.body()
    }
}
