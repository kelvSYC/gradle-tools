package com.kelvsyc.gradle.gitea.actions

import com.kelvsyc.gradle.clients.AbstractClientBuildService
import com.kelvsyc.gradle.gitea.GiteaService
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] that downloads release assets by name from a Gitea release.
 *
 * Fetches the release for the given tag and downloads all assets whose names match the provided list.
 */
abstract class DownloadGiteaReleaseArtifactAction :
    WorkAction<DownloadGiteaReleaseArtifactAction.Parameters> {
    /**
     * Parameters for [DownloadGiteaReleaseArtifactAction].
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
         * The Git tag identifying the release.
         */
        @get:Input
        val tag: Property<String>

        /**
         * The asset names to download (exact match).
         */
        @get:Input
        val assetNames: ListProperty<String>

        /**
         * The directory to download assets to.
         */
        @get:OutputDirectory
        val outputDirectory: DirectoryProperty
    }

    override fun execute() {
        val releaseResponse = parameters.service.get().getClient().getReleaseByTag(
            owner = parameters.owner.get(),
            repo = parameters.repo.get(),
            tag = parameters.tag.get(),
        ).execute()

        if (!releaseResponse.isSuccessful) {
            error("Failed to fetch release: ${releaseResponse.code()} ${releaseResponse.message()}")
        }

        val release = releaseResponse.body() ?: error("Release not found for tag: ${parameters.tag.get()}")
        val assets = release.assets ?: emptyList()
        val assetNameSet = parameters.assetNames.get().toSet()
        val matchingAssets = assets.filter { it.name in assetNameSet }

        for (asset in matchingAssets) {
            val name = asset.name ?: error("Asset name is null")
            val url = asset.browserDownloadUrl ?: error("Asset download URL is null for asset $name")
            val downloadResponse = parameters.service.get().getClient().downloadAsset(url).execute()

            if (!downloadResponse.isSuccessful) {
                error("Failed to download asset $name: ${downloadResponse.code()} ${downloadResponse.message()}")
            }

            val outputPath = parameters.outputDirectory.file(name).get().asFile
            downloadResponse.body()!!.byteStream().use { input ->
                outputPath.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
    }
}
