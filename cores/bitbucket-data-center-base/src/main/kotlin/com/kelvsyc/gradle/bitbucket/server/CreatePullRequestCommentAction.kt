package com.kelvsyc.gradle.bitbucket.server

import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.api.tasks.Internal

/**
 * [WorkAction] that creates a comment on a pull request in Bitbucket Data Center.
 */
abstract class CreatePullRequestCommentAction :
    WorkAction<CreatePullRequestCommentAction.Parameters> {
    /**
     * Parameters for [CreatePullRequestCommentAction].
     */
    interface Parameters : WorkParameters {
        /**
         * The shared [ClientsBaseService] holding the registered Bitbucket Data Center client.
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

        /**
         * The comment text.
         */
        val text: Property<String>
    }

    override fun execute() {
        val body = mapOf<String, Any>("text" to parameters.text.get())

        val response = parameters.service.get().getClient().createPullRequestComment(
            projectKey = parameters.projectKey.get(),
            repoSlug = parameters.repoSlug.get(),
            pullRequestId = parameters.pullRequestId.get(),
            body = body,
        ).execute()

        if (!response.isSuccessful) {
            error("Failed to create pull request comment: ${response.code()} ${response.message()}")
        }
    }
}
