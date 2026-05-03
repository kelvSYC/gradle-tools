package com.kelvsyc.gradle.gitlab.actions

import com.kelvsyc.gradle.git.which
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.process.ExecOperations
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import javax.inject.Inject

/**
 * [WorkAction] implementation retrieving the contents of a GitLab repository as an archive, using the GitLab CLI.
 */
abstract class GitLabRepoArchiveAction @Inject constructor(
    private val exec: ExecOperations, private val providers: ProviderFactory
) : WorkAction<GitLabRepoArchiveAction.Parameters> {
    /**
     * Parameters for [GitLabRepoArchiveAction].
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
         * The commit ref (SHA, branch, or tag) to archive.
         */
        val ref: Property<String>

        /**
         * The output file. The extension determines the archive format: `.tar.gz`/`.tgz` produces a gzip tarball,
         * `.tar.bz2`/`.tbz2` produces a bzip2 tarball, `.tar` produces an uncompressed tarball, `.zip` produces a
         * zip archive.
         */
        val outputFile: RegularFileProperty
    }

    private val glabCommandInternal = parameters.glabCommand.orElse(providers.which("glab"))
    private val ownerRepoInternal = parameters.owner.zip(parameters.repo) { owner, repo -> "$owner/$repo" }
    private val repoInternal = parameters.hostname.zip(ownerRepoInternal) { hostname, ownerRepo ->
        "$hostname/$ownerRepo"
    }.orElse(ownerRepoInternal)
    private val authEnvironment = parameters.token.map { mapOf("GITLAB_TOKEN" to it) }

    override fun execute() {
        val outputFile = parameters.outputFile.get().asFile
        val format = when {
            outputFile.name.endsWith(".tar.gz") || outputFile.name.endsWith(".tgz") -> "tar.gz"
            outputFile.name.endsWith(".tar.bz2") || outputFile.name.endsWith(".tbz2") -> "tar.bz2"
            outputFile.name.endsWith(".tar") -> "tar"
            outputFile.name.endsWith(".zip") -> "zip"
            else -> error("Unsupported archive format: ${outputFile.name}")
        }
        val args = buildList {
            add("repo")
            add("archive")
            add("--repo")
            add(repoInternal.get())
            add("--sha")
            add(parameters.ref.get())
            add("--format")
            add(format)
            add("--output")
            add(outputFile.absolutePath)
        }
        exec.exec {
            executable(glabCommandInternal.get())
            environment(authEnvironment.getOrElse(emptyMap()))
            args(args)
        }
    }
}
