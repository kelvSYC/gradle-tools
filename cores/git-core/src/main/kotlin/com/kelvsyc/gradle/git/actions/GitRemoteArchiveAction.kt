package com.kelvsyc.gradle.git.actions

import com.kelvsyc.gradle.git.which
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.process.ExecOperations
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import javax.inject.Inject

/**
 * [WorkAction] implementation retrieving the contents of a remote Git repository using `git archive --remote`.
 */
abstract class GitRemoteArchiveAction @Inject constructor(
    private val exec: ExecOperations, providers: ProviderFactory
) : WorkAction<GitRemoteArchiveAction.Parameters> {
    /**
     * Parameters for [GitRemoteArchiveAction].
     */
    interface Parameters : WorkParameters {
        /**
         * The underlying Git command.
         */
        val gitCommand: Property<String>

        /**
         * The URL to the remote repo.
         */
        val remoteUrl: Property<String>

        /**
         * The commit ref of the remote repo.
         */
        val ref: Property<String>

        /**
         * Paths within the repo to include. Leave empty to include all files and directories.
         */
        val paths: ListProperty<String>

        /**
         * The output file. The file must be of a format recognized by `git archive --list`.
         *
         * By default, `tar`, `zip`, `tar.gz`/`tgz` are supported.
         */
        val outputFile: RegularFileProperty

        /**
         * Determines whether or not verbose output is enabled. Defaults to `false`.
         */
        val verbose: Property<Boolean>
    }

    private val gitCommandInternal = parameters.gitCommand.orElse(providers.which("git"))

    override fun execute() {
        val args = buildList {
            add("archive")
            add("--remote=${parameters.remoteUrl.get()}")
            add("--output=${parameters.outputFile.get().asFile.absolutePath}")
            if (parameters.verbose.getOrElse(false)) {
                add("--verbose")
            }
            add(parameters.ref.get())
            addAll(parameters.paths.getOrElse(emptyList()))
        }

        exec.exec {
            executable(gitCommandInternal.get())
            args(args)
        }
    }
}
