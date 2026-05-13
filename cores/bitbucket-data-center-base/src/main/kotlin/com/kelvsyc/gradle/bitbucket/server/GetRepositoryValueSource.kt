package com.kelvsyc.gradle.bitbucket.server

import com.kelvsyc.gradle.bitbucket.server.model.Repository
import org.gradle.api.provider.Property
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
        val service: Property<BitbucketServerClientBuildService>

        /**
         * The Bitbucket project key.
         */
        val projectKey: Property<String>

        /**
         * The repository slug.
         */
        val repoSlug: Property<String>
    }

    override fun obtain(): Repository? {
        val response = parameters.service.get().getClient().getRepository(
            projectKey = parameters.projectKey.get(),
            repoSlug = parameters.repoSlug.get(),
        ).execute()

        return response.body()
    }
}
