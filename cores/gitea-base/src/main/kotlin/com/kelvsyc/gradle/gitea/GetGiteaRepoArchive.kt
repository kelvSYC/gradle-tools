package com.kelvsyc.gradle.gitea

import com.kelvsyc.gradle.clients.AbstractClientBuildService
import com.kelvsyc.gradle.gitea.actions.GetGiteaRepoArchiveAction
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import org.gradle.kotlin.dsl.submit
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * Task that downloads a repository archive from Gitea.
 *
 * The implementation delegates to [GetGiteaRepoArchiveAction] via the Gradle worker API for isolated execution.
 */
@DisableCachingByDefault(because = "Downloading from a remote repository is not cacheable")
abstract class GetGiteaRepoArchive @Inject constructor(private val workers: WorkerExecutor) : DefaultTask() {
    /**
     * The Gitea build service providing an authenticated client.
     */
    @get:Internal
    abstract val service: Property<AbstractClientBuildService<GiteaService, *>>

    /**
     * The owner (user or organization) of the repository.
     */
    @get:Input
    abstract val owner: Property<String>

    /**
     * The repository name.
     */
    @get:Input
    abstract val repo: Property<String>

    /**
     * The Git ref (branch, tag, or SHA) to archive.
     */
    @get:Input
    abstract val ref: Property<String>

    /**
     * The destination archive file. Format is inferred from the file extension:
     * `.tar.gz` or `.tgz` produces a tarball, `.zip` produces a ZIP archive.
     */
    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun execute() {
        workers.noIsolation().submit(GetGiteaRepoArchiveAction::class.java) {
            service.set(this@GetGiteaRepoArchive.service)
            owner.set(this@GetGiteaRepoArchive.owner)
            repo.set(this@GetGiteaRepoArchive.repo)
            ref.set(this@GetGiteaRepoArchive.ref)
            outputFile.set(this@GetGiteaRepoArchive.outputFile)
        }
    }
}
