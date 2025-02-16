package com.kelvsyc.gradle.git

import com.kelvsyc.gradle.git.actions.GitRemoteArchiveAction
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.submit
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * Task retrieving portions of a remote Git repository, using `git archive --remote`.
 *
 * The implementation of this task delegates to [GitRemoteArchiveAction].
 */
abstract class GetGitRemoteArchive @Inject constructor(private val workers: WorkerExecutor) : DefaultTask() {
    /**
     * The underlying Git command.
     *
     * @see [GitRemoteArchiveAction.Parameters.gitCommand]
     */
    @get:Internal
    abstract val gitCommand: Property<String>

    /**
     * The URL to the remote repo.
     *
     * @see [GitRemoteArchiveAction.Parameters.remoteUrl]
     */
    @get:Input
    abstract val remoteUrl: Property<String>

    /**
     * The commit ref of the remote repo.
     *
     * @see [GitRemoteArchiveAction.Parameters.ref]
     */
    @get:Input
    abstract val ref: Property<String>

    /**
     * Paths within the repo to include. Leave empty to include all files and directories.
     *
     * @see [GitRemoteArchiveAction.Parameters.paths]
     */
    @get:Input @get:Optional
    abstract val paths: ListProperty<String>

    /**
     * The output file. The file must be of a format recognized by `git archive --list`.
     *
     * By default, `tar`, `zip`, `tar.gz`/`tgz` are supported.
     *
     * @see [GitRemoteArchiveAction.Parameters.outputFile]
     */
    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    /**
     * Determines whether or not verbose output is enabled. Defaults to `false`.
     *
     * @see [GitRemoteArchiveAction.Parameters.verbose]
     */
    @get:Internal
    abstract val verbose: Property<Boolean>

    @TaskAction
    fun run() {
        val queue = workers.noIsolation()
        queue.submit(GitRemoteArchiveAction::class) {
            gitCommand.set(this@GetGitRemoteArchive.gitCommand)
            remoteUrl.set(this@GetGitRemoteArchive.remoteUrl)
            ref.set(this@GetGitRemoteArchive.ref)
            paths.addAll(this@GetGitRemoteArchive.paths)

            outputFile.set(this@GetGitRemoteArchive.outputFile)
            verbose.set(this@GetGitRemoteArchive.verbose)
        }
    }
}
