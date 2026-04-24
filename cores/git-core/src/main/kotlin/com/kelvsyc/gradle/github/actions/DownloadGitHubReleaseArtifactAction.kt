package com.kelvsyc.gradle.github.actions

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
 * [WorkAction] implementation downloading an asset from a GitHub release.
 */
abstract class DownloadGitHubReleaseArtifactAction @Inject constructor(
    private val exec: ExecOperations, private val providers: ProviderFactory
) : WorkAction<DownloadGitHubReleaseArtifactAction.Parameters> {
    /**
     * Parameters for [DownloadGitHubReleaseArtifactAction].
     */
    interface Parameters : WorkParameters {
        /**
         * The underlying GitHub CLI command.
         */
        val ghCommand: Property<String>

        /**
         * The GitHub hostname. Leave blank to use GitHub.com
         */
        val hostname: Property<String>

        /**
         * The GitHub personal access token. This will be supplied as the `GH_TOKEN` or `GH_ENTERPRISE_TOKEN`
         * environment variable, depending on whether the hostname is set.
         */
        val token: Property<String>

        /**
         * The owner of the repository to download the asset from.
         */
        val owner: Property<String>

        /**
         * The repository to download the asset from.
         */
        val repo: Property<String>

        /**
         * The Git tag to download the asset from.
         */
        val tag: Property<String>

        /**
         * The glob patterns to match against the asset name.
         */
        val patternGlobs: ListProperty<String>

        /**
         * The directory to download the asset to.
         */
        val outputDirectory: DirectoryProperty
    }

    private val ghCommandInternal = parameters.ghCommand.orElse(providers.which("gh"))
    private val repoInternal = parameters.owner.zip(parameters.repo) { owner, repo -> "$owner/$repo" }
    private val authEnvironment = parameters.hostname.zip(parameters.token) { hostname, token ->
        mapOf("GH_ENTERPRISE_TOKEN" to token)
    }.orElse(parameters.token.map { mapOf("GH_TOKEN" to it) })

    override fun execute() {
        val args = buildList {
            add("release")
            add("download")
            add(parameters.tag.get())
            if (parameters.hostname.isPresent) {
                add("--hostname")
                add(parameters.hostname.get())
            }
            add("--repo")
            add(repoInternal.get())
            addAll(parameters.patternGlobs.map { it.flatMap { listOf("--pattern", it) } }.getOrElse(emptyList()))
            add("--dir")
            add(parameters.outputDirectory.get().asFile.absolutePath)
        }
        exec.exec {
            executable(ghCommandInternal.get())
            environment(authEnvironment.getOrElse(emptyMap()))
            args(args)
        }
    }
}
