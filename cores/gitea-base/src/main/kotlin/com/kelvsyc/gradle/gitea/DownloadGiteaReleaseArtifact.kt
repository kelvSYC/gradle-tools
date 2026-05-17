package com.kelvsyc.gradle.gitea

import com.kelvsyc.gradle.clients.AbstractClientBuildService
import com.kelvsyc.gradle.gitea.actions.DownloadGiteaReleaseArtifactAction
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import org.gradle.kotlin.dsl.submit
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * Task that downloads release assets from a Gitea release.
 *
 * The implementation delegates to [DownloadGiteaReleaseArtifactAction] via the Gradle worker API for isolated
 * execution.
 */
@DisableCachingByDefault(because = "Downloading from a remote repository is not cacheable")
abstract class DownloadGiteaReleaseArtifact @Inject constructor(private val workers: WorkerExecutor) :
    DefaultTask() {
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
     * The Git tag identifying the release.
     */
    @get:Input
    abstract val tag: Property<String>

    /**
     * The asset names to download (exact match).
     */
    @get:Input
    abstract val assetNames: ListProperty<String>

    /**
     * The directory to download assets to.
     */
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun execute() {
        workers.noIsolation().submit(DownloadGiteaReleaseArtifactAction::class.java) {
            service.set(this@DownloadGiteaReleaseArtifact.service)
            owner.set(this@DownloadGiteaReleaseArtifact.owner)
            repo.set(this@DownloadGiteaReleaseArtifact.repo)
            tag.set(this@DownloadGiteaReleaseArtifact.tag)
            assetNames.set(this@DownloadGiteaReleaseArtifact.assetNames)
            outputDirectory.set(this@DownloadGiteaReleaseArtifact.outputDirectory)
        }
    }
}
