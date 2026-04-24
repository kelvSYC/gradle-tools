package com.kelvsyc.gradle.github

import com.kelvsyc.gradle.github.actions.GitHubRepoArchiveAction
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import org.gradle.kotlin.dsl.submit
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * Task retrieving the contents of a GitHub repository as an archive, using the GitHub CLI.
 *
 * The implementation of this task delegates to [GitHubRepoArchiveAction].
 */
@DisableCachingByDefault(because = "Downloading from a remote repository is not cacheable")
abstract class GetGitHubRepoArchive @Inject constructor(private val workers: WorkerExecutor) : DefaultTask() {
    /**
     * The underlying GitHub CLI command.
     *
     * @see [GitHubRepoArchiveAction.Parameters.ghCommand]
     */
    @get:Internal
    abstract val ghCommand: Property<String>

    /**
     * The GitHub hostname. Leave blank to use GitHub.com.
     *
     * @see [GitHubRepoArchiveAction.Parameters.hostname]
     */
    @get:Input
    @get:Optional
    abstract val hostname: Property<String>

    /**
     * The GitHub personal access token.
     *
     * @see [GitHubRepoArchiveAction.Parameters.token]
     */
    @get:Internal
    abstract val token: Property<String>

    /**
     * The owner of the repository.
     *
     * @see [GitHubRepoArchiveAction.Parameters.owner]
     */
    @get:Input
    abstract val owner: Property<String>

    /**
     * The repository name.
     *
     * @see [GitHubRepoArchiveAction.Parameters.repo]
     */
    @get:Input
    abstract val repo: Property<String>

    /**
     * The commit ref of the remote repo.
     *
     * @see [GitHubRepoArchiveAction.Parameters.ref]
     */
    @get:Input
    abstract val ref: Property<String>

    /**
     * The output file. The file must be of a format recognized by the GitHub API.
     *
     * By default, `zip` and `tar.gz` are supported.
     *
     * @see [GitHubRepoArchiveAction.Parameters.outputFile]
     */
    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun run() {
        val queue = workers.noIsolation()
        queue.submit(GitHubRepoArchiveAction::class) {
            ghCommand.set(this@GetGitHubRepoArchive.ghCommand)
            hostname.set(this@GetGitHubRepoArchive.hostname)
            token.set(this@GetGitHubRepoArchive.token)
            owner.set(this@GetGitHubRepoArchive.owner)
            repo.set(this@GetGitHubRepoArchive.repo)
            ref.set(this@GetGitHubRepoArchive.ref)
            outputFile.set(this@GetGitHubRepoArchive.outputFile)
        }
    }
}
