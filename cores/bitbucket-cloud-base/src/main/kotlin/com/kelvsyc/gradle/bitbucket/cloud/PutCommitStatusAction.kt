package com.kelvsyc.gradle.bitbucket.cloud

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] that creates or updates a build status on a commit in Bitbucket Cloud.
 *
 * The [Parameters.state] must be one of `SUCCESSFUL`, `FAILED`, `INPROGRESS`, or `STOPPED`.
 */
abstract class PutCommitStatusAction : WorkAction<PutCommitStatusAction.Parameters> {
    /**
     * Parameters for [PutCommitStatusAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the Bitbucket Cloud client. */
        @get:Internal
        val service: Property<BitbucketCloudClientBuildService>

        /** The Bitbucket workspace slug or UUID. */
        val workspace: Property<String>

        /** The repository slug. */
        val repoSlug: Property<String>

        /** The full commit hash to attach the status to. */
        val commit: Property<String>

        /** A unique key identifying this build status (e.g. the build pipeline name). */
        val key: Property<String>

        /** The build state. Must be one of `SUCCESSFUL`, `FAILED`, `INPROGRESS`, or `STOPPED`. */
        val state: Property<String>

        /** A URL linking to the build results. */
        val url: Property<String>

        /** A short human-readable name for the build status. */
        val name: Property<String>

        /** A description of the build status. */
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

        val response = parameters.service.get().getClient().putCommitStatus(
            workspace = parameters.workspace.get(),
            repoSlug = parameters.repoSlug.get(),
            commit = parameters.commit.get(),
            key = parameters.key.get(),
            body = body,
        ).execute()

        if (!response.isSuccessful) {
            error("Failed to update commit status: ${response.code()} ${response.message()}")
        }
    }
}
