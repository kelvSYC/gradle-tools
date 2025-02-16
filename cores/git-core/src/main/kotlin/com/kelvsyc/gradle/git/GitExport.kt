package com.kelvsyc.gradle.git

import com.kelvsyc.gradle.git.actions.GitRemoteArchiveAction
import org.gradle.api.DefaultTask
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.submit
import org.gradle.workers.WorkerExecutor
import java.io.File
import javax.inject.Inject

/**
 * Task that downloads files from a specific commit within a remote repository to a specific directory, using
 * `git archive --remote`.
 *
 * This task is functionally similar to [GetGitRemoteArchive], except that the resulting archive is extracted to the
 * [outputDirectory]. Like [GetGitRemoteArchive], this task is implemented using [GitRemoteArchiveAction].
 */
abstract class GitExport @Inject constructor(
    private val workers: WorkerExecutor,
    private val ar: ArchiveOperations,
    private val fs: FileSystemOperations
) : DefaultTask() {
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
    @get:Input
    @get:Optional
    abstract val paths: ListProperty<String>

    /**
     * Output directory
     */
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    /**
     * Determines whether or not verbose output is enabled. Defaults to `false`.
     *
     * @see [GitRemoteArchiveAction.Parameters.verbose]
     */
    @get:Internal
    abstract val verbose: Property<Boolean>

    private val tempZip = File(temporaryDir, "export.zip")

    @TaskAction
    fun run() {
        val queue = workers.noIsolation()
        queue.submit(GitRemoteArchiveAction::class) {
            gitCommand.set(this@GitExport.gitCommand)
            remoteUrl.set(this@GitExport.remoteUrl)
            ref.set(this@GitExport.ref)
            paths.addAll(this@GitExport.paths)

            outputFile.set(tempZip)
            verbose.set(this@GitExport.verbose)
        }

        // Extract the temporary zip file
        fs.copy {
            from(ar.zipTree(tempZip))
            into(outputDirectory)
        }
    }
}
