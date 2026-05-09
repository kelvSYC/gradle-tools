package com.kelvsyc.gradle.bitbucket.cloud

import com.kelvsyc.gradle.bitbucket.cloud.model.Repository
import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters

/**
 * [ValueSource] that fetches repository metadata from the Bitbucket Cloud API.
 */
abstract class GetRepositoryValueSource :
    ValueSource<Repository, GetRepositoryValueSource.Parameters> {
    /**
     * Parameters for [GetRepositoryValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The shared build service managing Bitbucket Cloud clients.
         */
        val service: Property<ClientsBaseService>

        /**
         * Registered name of a [BitbucketCloudClientInfo].
         */
        val clientName: Property<String>

        /**
         * The Bitbucket workspace slug or UUID.
         */
        val workspace: Property<String>

        /**
         * The repository slug.
         */
        val repoSlug: Property<String>
    }

    private val client: Provider<BitbucketCloudService> =
        parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun obtain(): Repository? {
        val response = client.get().getRepository(
            workspace = parameters.workspace.get(),
            repoSlug = parameters.repoSlug.get(),
        ).execute()

        return response.body()
    }
}
