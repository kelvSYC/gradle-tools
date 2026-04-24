package com.kelvsyc.gradle.github.actions

import com.kelvsyc.gradle.git.which
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.process.ExecOperations
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import javax.inject.Inject

/**
 * [WorkAction] implementation retrieving the contents of a GitHub repository using the GitHub CLI.
 */
abstract class GitHubRepoArchiveAction @Inject constructor(
    private val exec: ExecOperations, private val providers: ProviderFactory
) : WorkAction<GitHubRepoArchiveAction.Parameters> {
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
         * The commit ref of the remote repo.
         */
        val ref: Property<String>

        /**
         * The output file. The extension determines the archive format: `.tar.gz`/`.tgz` downloads a
         * tarball, `.zip` downloads a zip archive.
         */
        val outputFile: RegularFileProperty
    }

    private val ghCommandInternal = parameters.ghCommand.orElse(providers.which("gh"))
    private val authEnvironment = parameters.hostname.zip(parameters.token) { _, token ->
        mapOf("GH_ENTERPRISE_TOKEN" to token)
    }.orElse(parameters.token.map { mapOf("GH_TOKEN" to it) })

    override fun execute() {
        val outputFile = parameters.outputFile.get().asFile
        val archiveFormat = when {
            outputFile.name.endsWith(".tar.gz") || outputFile.name.endsWith(".tgz") -> "tarball"
            outputFile.name.endsWith(".zip") -> "zipball"
            else -> error("Unsupported archive format: ${outputFile.name}")
        }
        val endpoint = "/repos/${parameters.owner.get()}/${parameters.repo.get()}/$archiveFormat/${parameters.ref.get()}"
        val args = buildList {
            add("api")
            add(endpoint)
            if (parameters.hostname.isPresent) {
                add("--hostname")
                add(parameters.hostname.get())
            }
        }
        outputFile.outputStream().use { out ->
            exec.exec {
                executable(ghCommandInternal.get())
                environment(authEnvironment.getOrElse(emptyMap()))
                args(args)
                standardOutput = out
            }
        }
    }
}
