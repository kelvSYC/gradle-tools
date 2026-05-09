package com.kelvsyc.gradle.bitbucket.cloud

import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] that creates a comment on a pull request in Bitbucket Cloud.
 */
abstract class CreatePullRequestCommentAction :
    WorkAction<CreatePullRequestCommentAction.Parameters> {
    /**
     * Parameters for [CreatePullRequestCommentAction].
     */
    interface Parameters : WorkParameters {
        /**
         * The shared [ClientsBaseService] holding the registered Bitbucket Cloud client.
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

        /**
         * The comment body, in Markdown format.
         */
        val body: Property<String>
    }

    private val client: Provider<BitbucketCloudService> =
        parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun execute() {
        val requestBody = mapOf<String, Any>(
            "content" to mapOf("raw" to parameters.body.get()),
        )

        val response = client.get().createPullRequestComment(
            workspace = parameters.workspace.get(),
            repoSlug = parameters.repoSlug.get(),
            pullRequestId = parameters.pullRequestId.get(),
            body = requestBody,
        ).execute()

        if (!response.isSuccessful) {
            error("Failed to create pull request comment: ${response.code()} ${response.message()}")
        }
    }
}
