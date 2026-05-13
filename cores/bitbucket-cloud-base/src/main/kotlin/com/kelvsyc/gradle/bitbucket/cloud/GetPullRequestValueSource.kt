package com.kelvsyc.gradle.bitbucket.cloud

import com.kelvsyc.gradle.bitbucket.cloud.model.PullRequest
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] that fetches a single pull request from the Bitbucket Cloud API.
 */
abstract class GetPullRequestValueSource :
    ValueSource<PullRequest, GetPullRequestValueSource.Parameters> {
    /**
     * Parameters for [GetPullRequestValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the Bitbucket Cloud client. */
        @get:Internal
        val service: Property<BitbucketCloudClientBuildService>

        /** The Bitbucket workspace slug or UUID. */
        val workspace: Property<String>

        /** The repository slug. */
        val repoSlug: Property<String>

        /** The pull request ID. */
        val pullRequestId: Property<Long>
    }

    override fun obtain(): PullRequest? {
        val response = parameters.service.get().getClient().getPullRequest(
            workspace = parameters.workspace.get(),
            repoSlug = parameters.repoSlug.get(),
            pullRequestId = parameters.pullRequestId.get(),
        ).execute()

        return response.body()
    }
}
