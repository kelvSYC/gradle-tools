package com.kelvsyc.gradle.gitea.actions

import com.kelvsyc.gradle.clients.AbstractClientBuildService
import com.kelvsyc.gradle.gitea.GiteaService
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] that creates a comment on a pull request in Gitea.
 */
abstract class CreatePullRequestCommentAction : WorkAction<CreatePullRequestCommentAction.Parameters> {
    /**
     * Parameters for [CreatePullRequestCommentAction].
     */
    interface Parameters : WorkParameters {
        /**
         * The build service managing the Gitea client.
         */
        @get:Internal
        val service: Property<AbstractClientBuildService<GiteaService, *>>

        /**
         * The owner (user or organization) of the repository.
         */
        @get:Input
        val owner: Property<String>

        /**
         * The repository name.
         */
        @get:Input
        val repo: Property<String>

        /**
         * The pull request number (index).
         */
        @get:Input
        val index: Property<Long>

        /**
         * The comment body text.
         */
        @get:Input
        val body: Property<String>
    }

    override fun execute() {
        val requestBody = mapOf(
            "body" to parameters.body.get(),
        )

        val response = parameters.service.get().getClient().createComment(
            owner = parameters.owner.get(),
            repo = parameters.repo.get(),
            index = parameters.index.get(),
            body = requestBody,
        ).execute()

        if (!response.isSuccessful) {
            error("Failed to create pull request comment: ${response.code()} ${response.message()}")
        }
    }
}
