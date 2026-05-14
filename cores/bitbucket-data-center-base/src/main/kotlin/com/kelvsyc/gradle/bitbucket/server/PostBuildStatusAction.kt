package com.kelvsyc.gradle.bitbucket.server

import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.api.tasks.Internal

/**
 * [WorkAction] that posts a build status to a commit in Bitbucket Data Center.
 *
 * The [Parameters.state] must be one of `SUCCESSFUL`, `FAILED`, or `INPROGRESS`.
 */
abstract class PostBuildStatusAction : WorkAction<PostBuildStatusAction.Parameters> {
    /**
     * Parameters for [PostBuildStatusAction].
     */
    interface Parameters : WorkParameters {
        /**
         * The [BitbucketServerClientBuildService] for this operation.
         */
        @get:Internal
        val service: Property<BitbucketServerClientBuildService>

        /**
         * The full commit hash to attach the status to.
         */
        val commitId: Property<String>

        /**
         * The build state. Must be one of `SUCCESSFUL`, `FAILED`, or `INPROGRESS`.
         */
        val state: Property<String>

        /**
         * A unique key identifying this build status (e.g. the build pipeline name).
         */
        val key: Property<String>

        /**
         * A URL linking to the build results.
         */
        val url: Property<String>

        /**
         * A short human-readable name for the build status.
         */
        val name: Property<String>

        /**
         * A description of the build status.
         */
        val description: Property<String>
    }

    override fun execute() {
        val body = buildMap<String, Any> {
            put("state", parameters.state.get())
            put("key", parameters.key.get())
            put("url", parameters.url.get())
            parameters.name.orNull?.let { put("name", it) }
            parameters.description.orNull?.let { put("description", it) }
        }

        val response = parameters.service.get().getClient().postBuildStatus(
            commitId = parameters.commitId.get(),
            body = body,
        ).execute()

        if (!response.isSuccessful) {
            error("Failed to post build status: ${response.code()} ${response.message()}")
        }
    }
}
