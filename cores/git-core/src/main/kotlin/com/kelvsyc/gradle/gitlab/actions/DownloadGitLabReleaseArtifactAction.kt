package com.kelvsyc.gradle.gitlab.actions

import com.kelvsyc.gradle.git.which
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.process.ExecOperations
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import javax.inject.Inject

/**
 * [WorkAction] implementation downloading an asset from a GitLab release, using the GitLab CLI.
 */
abstract class DownloadGitLabReleaseArtifactAction @Inject constructor(
    private val exec: ExecOperations, private val providers: ProviderFactory
) : WorkAction<DownloadGitLabReleaseArtifactAction.Parameters> {
    /**
     * Parameters for [DownloadGitLabReleaseArtifactAction].
     */
    interface Parameters : WorkParameters {
        /**
         * The underlying GitLab CLI command.
         */
        val glabCommand: Property<String>

        /**
         * The GitLab hostname. Leave blank to use GitLab.com.
         *
         * When set, the hostname is prepended to the repository path as `hostname/owner/repo`.
         */
        val hostname: Property<String>

        /**
         * The GitLab personal access token, supplied as the `GITLAB_TOKEN` environment variable.
         */
        val token: Property<String>

        /**
         * The namespace or group owning the repository.
         */
        val owner: Property<String>

        /**
         * The repository name.
         */
        val repo: Property<String>

        /**
         * The release tag to download assets from.
         */
        val tag: Property<String>

        /**
         * Asset name filters. Each entry is passed as a separate `--asset-name` argument and supports glob patterns.
         * Leave empty to download all assets.
         */
        val assetNames: ListProperty<String>

        /**
         * The directory to download assets into.
         */
        val outputDirectory: DirectoryProperty
    }

    private val glabCommandInternal = parameters.glabCommand.orElse(providers.which("glab"))
    private val ownerRepoInternal = parameters.owner.zip(parameters.repo) { owner, repo -> "$owner/$repo" }
    private val repoInternal = parameters.hostname.zip(ownerRepoInternal) { hostname, ownerRepo ->
        "$hostname/$ownerRepo"
    }.orElse(ownerRepoInternal)
    private val authEnvironment = parameters.token.map { mapOf("GITLAB_TOKEN" to it) }

    override fun execute() {
        val args = buildList {
            add("release")
            add("download")
            add(parameters.tag.get())
            add("--repo")
            add(repoInternal.get())
            addAll(parameters.assetNames.map { it.flatMap { name -> listOf("--asset-name", name) } }.getOrElse(emptyList()))
            add("--dir")
            add(parameters.outputDirectory.get().asFile.absolutePath)
        }
        exec.exec {
            executable(glabCommandInternal.get())
            environment(authEnvironment.getOrElse(emptyMap()))
            args(args)
        }
    }
}
