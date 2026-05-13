package com.kelvsyc.gradle.bitbucket.cloud

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
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
        /** The build service managing the Bitbucket Cloud client. */
        @get:Internal
        val service: Property<BitbucketCloudClientBuildService>

        /** The Bitbucket workspace slug or UUID. */
        val workspace: Property<String>

        /** The repository slug. */
        val repoSlug: Property<String>

        /** The pull request ID. */
        val pullRequestId: Property<Long>

        /** The comment body, in Markdown format. */
        val body: Property<String>
    }

    override fun execute() {
        val requestBody = mapOf<String, Any>(
            "content" to mapOf("raw" to parameters.body.get()),
        )

        val response = parameters.service.get().getClient().createPullRequestComment(
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
