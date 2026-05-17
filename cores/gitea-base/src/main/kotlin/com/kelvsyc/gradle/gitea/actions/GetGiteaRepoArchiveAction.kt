package com.kelvsyc.gradle.gitea.actions

import com.kelvsyc.gradle.clients.AbstractClientBuildService
import com.kelvsyc.gradle.gitea.GiteaService
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] that downloads a repository archive from Gitea.
 *
 * The archive format (tar.gz or zip) is determined automatically from the output file extension.
 */
abstract class GetGiteaRepoArchiveAction : WorkAction<GetGiteaRepoArchiveAction.Parameters> {
    /**
     * Parameters for [GetGiteaRepoArchiveAction].
     */
    interface Parameters : WorkParameters {
        /**
         * The build service managing the Gitea client.
         */
        @get:Internal
        val service: Property<AbstractClientBuildService<GiteaService, *>>

        /**
         * The owner (user or organization) of the repository.
         */
        @get:Input
        val owner: Property<String>

        /**
         * The repository name.
         */
        @get:Input
        val repo: Property<String>

        /**
         * The Git ref (branch, tag, or SHA) to archive.
         */
        @get:Input
        val ref: Property<String>

        /**
         * The destination archive file. The format is inferred from the file extension:
         * `.tar.gz` or `.tgz` produces a tarball, `.zip` produces a ZIP archive.
         */
        @get:OutputFile
        val outputFile: RegularFileProperty
    }

    override fun execute() {
        val outputFile = parameters.outputFile.get().asFile
        val format = when {
            outputFile.name.endsWith(".tar.gz") || outputFile.name.endsWith(".tgz") -> "tar.gz"
            outputFile.name.endsWith(".zip") -> "zip"
            else -> error("Unsupported archive format: ${outputFile.name}")
        }
        val filepath = "${parameters.ref.get()}.$format"

        val response = parameters.service.get().getClient().getArchive(
            owner = parameters.owner.get(),
            repo = parameters.repo.get(),
            filepath = filepath,
        ).execute()

        if (!response.isSuccessful) {
            error("Failed to download repository archive: ${response.code()} ${response.message()}")
        }

        response.body()!!.byteStream().use { input ->
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
}
