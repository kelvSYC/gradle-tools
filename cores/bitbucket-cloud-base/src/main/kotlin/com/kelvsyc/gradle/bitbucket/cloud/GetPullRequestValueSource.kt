package com.kelvsyc.gradle.bitbucket.cloud

import com.kelvsyc.gradle.bitbucket.cloud.model.PullRequest
import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters

/**
 * [ValueSource] that fetches a single pull request from the Bitbucket Cloud API.
 */
abstract class GetPullRequestValueSource :
    ValueSource<PullRequest, GetPullRequestValueSource.Parameters> {
    /**
     * Parameters for [GetPullRequestValueSource].
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

        /**
         * The pull request ID.
         */
        val pullRequestId: Property<Long>
    }

    private val client: Provider<BitbucketCloudService> =
        parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun obtain(): PullRequest? {
        val response = client.get().getPullRequest(
            workspace = parameters.workspace.get(),
            repoSlug = parameters.repoSlug.get(),
            pullRequestId = parameters.pullRequestId.get(),
        ).execute()

        return response.body()
    }
}
