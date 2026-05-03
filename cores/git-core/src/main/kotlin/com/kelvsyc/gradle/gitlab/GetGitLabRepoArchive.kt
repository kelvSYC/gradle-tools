package com.kelvsyc.gradle.gitlab

import com.kelvsyc.gradle.gitlab.actions.GitLabRepoArchiveAction
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.submit
import org.gradle.work.DisableCachingByDefault
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * Task retrieving the contents of a GitLab repository as an archive, using the GitLab CLI.
 *
 * The implementation of this task delegates to [GitLabRepoArchiveAction].
 */
@DisableCachingByDefault(because = "Downloading from a remote repository is not cacheable")
abstract class GetGitLabRepoArchive @Inject constructor(private val workers: WorkerExecutor) : DefaultTask() {
    /**
     * The underlying GitLab CLI command.
     *
     * @see [GitLabRepoArchiveAction.Parameters.glabCommand]
     */
    @get:Internal
    abstract val glabCommand: Property<String>

    /**
     * The GitLab hostname. Leave blank to use GitLab.com.
     *
     * @see [GitLabRepoArchiveAction.Parameters.hostname]
     */
    @get:Input
    @get:Optional
    abstract val hostname: Property<String>

    /**
     * The GitLab personal access token.
     *
     * @see [GitLabRepoArchiveAction.Parameters.token]
     */
    @get:Internal
    abstract val token: Property<String>

    /**
     * The namespace or group owning the repository.
     *
     * @see [GitLabRepoArchiveAction.Parameters.owner]
     */
    @get:Input
    abstract val owner: Property<String>

    /**
     * The repository name.
     *
     * @see [GitLabRepoArchiveAction.Parameters.repo]
     */
    @get:Input
    abstract val repo: Property<String>

    /**
     * The commit ref to archive.
     *
     * @see [GitLabRepoArchiveAction.Parameters.ref]
     */
    @get:Input
    abstract val ref: Property<String>

    /**
     * The output file. The extension determines the archive format.
     *
     * Supported formats: `.tar.gz`/`.tgz`, `.tar.bz2`/`.tbz2`, `.tar`, `.zip`.
     *
     * @see [GitLabRepoArchiveAction.Parameters.outputFile]
     */
    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun run() {
        val queue = workers.noIsolation()
        queue.submit(GitLabRepoArchiveAction::class) {
            glabCommand.set(this@GetGitLabRepoArchive.glabCommand)
            hostname.set(this@GetGitLabRepoArchive.hostname)
            token.set(this@GetGitLabRepoArchive.token)
            owner.set(this@GetGitLabRepoArchive.owner)
            repo.set(this@GetGitLabRepoArchive.repo)
            ref.set(this@GetGitLabRepoArchive.ref)
            outputFile.set(this@GetGitLabRepoArchive.outputFile)
        }
    }
}
