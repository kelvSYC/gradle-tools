package com.kelvsyc.gradle.bitbucket.cloud

import com.kelvsyc.gradle.bitbucket.cloud.model.Repository
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] that fetches repository metadata from the Bitbucket Cloud API.
 */
abstract class GetRepositoryValueSource :
    ValueSource<Repository, GetRepositoryValueSource.Parameters> {
    /**
     * Parameters for [GetRepositoryValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the Bitbucket Cloud client. */
        @get:Internal
        val service: Property<BitbucketCloudClientBuildService>

        /** The Bitbucket workspace slug or UUID. */
        val workspace: Property<String>

        /** The repository slug. */
        val repoSlug: Property<String>
    }

    override fun obtain(): Repository? {
        val response = parameters.service.get().getClient().getRepository(
            workspace = parameters.workspace.get(),
            repoSlug = parameters.repoSlug.get(),
        ).execute()

        return response.body()
    }
}
