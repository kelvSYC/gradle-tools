package com.kelvsyc.gradle.gitea.actions

import com.kelvsyc.gradle.clients.AbstractClientBuildService
import com.kelvsyc.gradle.gitea.GiteaService
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] that posts a commit status to the Gitea API.
 *
 * The status is created or updated on the specified commit SHA with a state, description, and context.
 */
abstract class PostCommitStatusAction : WorkAction<PostCommitStatusAction.Parameters> {
    /**
     * Parameters for [PostCommitStatusAction].
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
         * The commit SHA to attach the status to.
         */
        @get:Input
        val sha: Property<String>

        /**
         * The status state. Must be one of: "pending", "success", "error", "failure", or "warning".
         */
        @get:Input
        val state: Property<String>

        /**
         * The target URL linking to the status details (e.g. build logs). Optional.
         */
        @get:Input
        @get:Optional
        val targetUrl: Property<String>

        /**
         * A description of the status. Optional.
         */
        @get:Input
        @get:Optional
        val description: Property<String>

        /**
         * The status context, a label identifying the status (e.g. "continuous-integration/build").
         */
        @get:Input
        val context: Property<String>
    }

    override fun execute() {
        val body = buildMap<String, String?> {
            put("state", parameters.state.get())
            put("context", parameters.context.get())
            parameters.targetUrl.orNull?.let { put("target_url", it) }
            parameters.description.orNull?.let { put("description", it) }
        }

        val response = parameters.service.get().getClient().createStatus(
            owner = parameters.owner.get(),
            repo = parameters.repo.get(),
            sha = parameters.sha.get(),
            body = body,
        ).execute()

        if (!response.isSuccessful) {
            error("Failed to post commit status: ${response.code()} ${response.message()}")
        }
    }
}
