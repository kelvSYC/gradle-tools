package com.kelvsyc.gradle.bitbucket.server

import com.kelvsyc.gradle.bitbucket.server.model.PullRequest
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] that fetches a single pull request from the Bitbucket Data Center API.
 */
abstract class GetPullRequestValueSource :
    ValueSource<PullRequest, GetPullRequestValueSource.Parameters> {
    /**
     * Parameters for [GetPullRequestValueSource].
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

        /**
         * The pull request ID.
         */
        val pullRequestId: Property<Long>
    }

    override fun obtain(): PullRequest? {
        val response = parameters.service.get().getClient().getPullRequest(
            projectKey = parameters.projectKey.get(),
            repoSlug = parameters.repoSlug.get(),
            pullRequestId = parameters.pullRequestId.get(),
        ).execute()

        return response.body()
    }
}
